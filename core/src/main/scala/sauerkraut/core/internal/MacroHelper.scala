package sauerkraut
package core
package internal


import scala.compiletime.{constValue, erasedValue, summonFrom}
import scala.deriving._
import scala.quoted._

/** Helpers for generation that are implemented as low-level macros. 
 * 
 * Creation of these shold be limited.
 */
object MacroHelper:
    

  /** Returns the field number for a compile-time string constant of a field name. */
  inline def fieldNum[P](fieldName: String): Int =
    ${fieldNumImplMacro[P]('fieldName)}

  /** 
   * Given a field number (as returned by `fieldNum`), returns the 0-indexed
   * order this field should be used in a case class constructor.
   */
  inline def fieldNumToConstructorOrder[P](fieldNum: Int): Int =
    ${fieldNumToConstructorImpl[P]('fieldNum)}

  inline def fieldName[P](num: Int): String = ???

  // Macro implementation for case-matching incoming field nums and getting index of name.
  private def fieldNumToConstructorImpl[P](using Type[P])(using Quotes)(fieldNum: Expr[Int]): Expr[Int] =
    import quotes.reflect._
    val allocations = caseFieldAllocations[P]
    val cases: Iterable[CaseDef] =
      for
        ((name, num), idx) <- allocations.zipWithIndex
      yield
        CaseDef(Literal(IntConstant(num)), None, Literal(IntConstant(idx)))
    // Match against the field num.
    Match(fieldNum.asTerm, cases.toList).asExpr.asInstanceOf[Expr[Int]]


  // Macro impelmentation for grabbing field number for compile-time name.
  private def fieldNumImplMacro[P](using Type[P])(using Quotes)(fieldNameExpr: Expr[String]): Expr[Int] =
    import quotes.reflect._
    // Here we build a consistent (idmepotent) set of field numbers for a given class, then
    // just return one of em for compilation.
    // TODO: Compile-time optimisations.
    fieldNameExpr.value match
      case Some(name) =>
        val lookup = caseFieldAllocations[P].toMap
        Expr(lookup(name))
      case _ => 
        // TODO - compile time exception
        report.error(s"Unable to find compile time fieldname from: ${fieldNameExpr.show}")
        '{???}


  /**
   * Determines (in side macros) the field <-> fieldnum assocaitions.
   * 
   * This is meant to be idempotent and consistent for any type allocated.
   */
  private def caseFieldAllocations[P](using Type[P])(using Quotes): Seq[(String, Int)] =
    import quotes.reflect._
    val ts = TypeTree.of[P].tpe.typeSymbol
    val initialAllocations: Seq[(String, Int)] = 
      for
        fieldSym <- ts.caseFields
        ValDef(name, tpe, optDefault) = fieldSym.tree
      yield
        //ts.caseFields.find(_.name == name).getOrElse(Symbol.noSymbol)
        tpe.tpe match
          // TODO - Ensure this is a `@field(num)` annotation. 
          case AnnotatedType(tpe, Apply(term, List(Literal(IntConstant(num))))) =>
            (fieldSym.name, num.asInstanceOf[Int])
          case _ => (fieldSym.name, 0)
    
    // Now, we assign fields with 0 to the next available integer NOT in our set.
    var nextInt = 1
    val taken = collection.mutable.HashSet[Int]()
    for
      (_, num) <- initialAllocations
      if num != 0
    do taken.add(num)
    // When encountering an unassinged field num, grab first available after incrmenting.
    def nextFieldNum(): Int =
      if (taken.contains(nextInt))
      then
        nextInt  += 1
        nextFieldNum()
      else
        taken.add(nextInt)
        nextInt
    // When encountering an assigned field num, make sure following fields follow after it.
    def moveNextInt(num: Int): Int =
      if nextInt <= num then
        nextInt = num + 1
      num

    for (field, num) <- initialAllocations
    yield 
      if num == 0
      then (field, nextFieldNum())
      else (field, moveNextInt(num))


