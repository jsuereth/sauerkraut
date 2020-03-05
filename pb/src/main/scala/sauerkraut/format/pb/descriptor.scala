/*
 * Copyright 2019 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sauerkraut
package format
package pb

/** An annotation for field numbers on a case class. */
class field(number: Int) extends scala.annotation.StaticAnnotation

/**
 * A descriptor for a protobuf type.
 * 
 * This is used during serialization/deserialization.
 */
trait ProtoTypeDescriptor[T]
  def tag: FastTypeTag[T]

/** A descriptor for a field in a message. */
final case class FieldDescriptor[T](
  name: String,
  number: Int,
  desc: ProtoTypeDescriptor[T])

/** A marker for a simple type. */
final case class SimpleTypeDescriptor[T](
    override val tag: FastTypeTag[T])
    extends ProtoTypeDescriptor[T]

/** Defines a message (key-value field pairs). */
final case class MessageTypeDescriptor[T](
    override val tag: FastTypeTag[T],
    val fields: List[FieldDescriptor[?]])
    extends ProtoTypeDescriptor[T]
  
  final def fieldByName(name: String): Option[FieldDescriptor[?]] =
    fields.find(_.name == name)
  final def fieldByNum(num: Int): Option[FieldDescriptor[?]] =
    fields.find(_.number == num)

/** 
 * A descriptor for how a Scala case class/enum matches
 * a protocol buffer definitiion.
 * 
 * This is used to serialize the class when available.
 */
trait TypeDescriptorMapping[T]
  /** Looks up a protocol buffer field number from field name. */
  def fieldNumber(name: String): Int
  /** Looks up the protocol buffer name from a field number. */
  def fieldName(num: Int): String
  /** Looks up a sub-type descriptor by field name. */
  def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]]

/** Companion object for TypeDescriptorMapping.  Allows derivation. */
object TypeDescriptorMapping
  inline def derived[T]: TypeDescriptorMapping[T] =
    new TypeDescriptorMapping[T] {
      override def fieldName(num: Int): String = lookupFieldName[T](num)
      override def fieldNumber(name: String): Int =
        lookupFieldNum[T](name)
      override def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]] =
        lookupFieldDescriptor[F, T](name)
    }
  import compiletime.{erasedValue,summonFrom}
  import scala.quoted._
  import scala.tasty._
  import deriving._
  import core.internal.InlineHelper._
  private inline def lookupFieldNum[T](name: String): Int =
    summonFrom {
      case m: Mirror.ProductOf[T] =>
        fieldNumCheck[m.type](name)
    }
  private inline def lookupFieldName[T](num: Int): String =
    summonFrom {
      case m: Mirror.ProductOf[T] =>
        fieldNameCheck[m.type](num)
    }
  private inline def lookupFieldDescriptor[F, T](name: String): Option[TypeDescriptorMapping[F]] =
    summonFrom {
      case m: Mirror.ProductOf[T] =>
        fieldDescLookup[F, m.type](name)
    }
  
  private inline def fieldNumCheck[T](name: String): Int =
    ${fieldNumCheckImpl[T]('name)}
  private inline def fieldNameCheck[T](num: Int): String =
    ${fieldNameCheckImpl[T]('num)}
  private inline def fieldDescLookup[F, T](name: String): Option[TypeDescriptorMapping[F]] =
    ${fieldDescLookupImpl[F, T]('name)}

  def fieldNameCheckImpl[T](using t: Type[T], qctx: QuoteContext)(id: Expr[Int]): Expr[String] =
    import qctx.tasty.{_, given _}
    import qctx._
    val helper = MacroHelper(qctx)
    val fieldNamesAndTypesWithNum =
      helper.fieldNamesTypesAndNumber(t.unseal.tpe.asInstanceOf[helper.qctx.tasty.Type])
    val cases: Iterable[CaseDef] =
      fieldNamesAndTypesWithNum map {
        case (label, (tpe, num)) =>
          CaseDef(Literal(Constant(num)), None, Literal(Constant(label))).asInstanceOf[qctx.tasty.CaseDef]
      }      
    // Effectively a pattern match against all known field names to return
    // the field numbers.
    Match(id.unseal, cases.toList).seal.asInstanceOf[Expr[String]]

  def fieldNumCheckImpl[T](using t: Type[T], qctx: QuoteContext)(name: Expr[String]): Expr[Int] =
    import qctx.tasty.{_, given _}
    import qctx._
    val helper = MacroHelper(qctx)
    val fieldNamesAndTypesWithNum =
      helper.fieldNamesTypesAndNumber(t.unseal.tpe.asInstanceOf[helper.qctx.tasty.Type])
    val cases: Iterable[CaseDef] =
      fieldNamesAndTypesWithNum map {
        case (label, (tpe, num)) =>
          CaseDef(Literal(Constant(label)), None, Literal(Constant(num))).asInstanceOf[qctx.tasty.CaseDef]
      }
    // Effectively a pattern match against all known field names to return
    // the field numbers.
    Match(name.unseal, cases.toList).seal.asInstanceOf[Expr[Int]]

  def fieldDescLookupImpl[F, T](using t: Type[T], 
                                      f: Type[F],
                                      qctx: QuoteContext)(name: Expr[String]): Expr[Option[TypeDescriptorMapping[F]]] =
    import qctx.tasty.{_, given}
    import qctx._
    val helper = MacroHelper(qctx)
    val fieldNamesAndTypesWithNum =
      helper.fieldNamesTypesAndNumber(t.unseal.tpe.asInstanceOf[helper.qctx.tasty.Type])
    inline def lookupDesc(fieldType: Type): Expr[Option[TypeDescriptorMapping[F]]] =
      val descType = AppliedType(typeOf[TypeDescriptorMapping[_]].asInstanceOf[AppliedType].tycon, List(fieldType)).seal
      matching.summonExpr(using descType.asInstanceOf) match {
        case Some(expr) => 
          '{Some(${expr})}
        case None =>
          // TODO - issue a warning if the type is not a primitive. 
          //qctx.warning(s"Cannot find given ${descType.show}")
          '{None}
      }
    val cases: Iterable[CaseDef] =
      fieldNamesAndTypesWithNum map {
        case (label, (tpe, num)) =>
          CaseDef(Literal(Constant(label)), 
                  None, 
                  lookupDesc(tpe.asInstanceOf).unseal)
      }
    Match(name.unseal, cases.toList).seal.asInstanceOf[Expr[Option[TypeDescriptorMapping[F]]]]


/**
 * A repository for type descriptors mappings that will be used
 * in serialization/deserialization.
 */ 
trait TypeDescriptorRepository
  def find[T](tag: FastTypeTag[T]): TypeDescriptorMapping[T]

private class TypeDescriptorRepositoryImpl(values: Map[FastTypeTag[?], TypeDescriptorMapping[?]]) 
    extends TypeDescriptorRepository
  override def find[T](tag: FastTypeTag[T]): TypeDescriptorMapping[T] =
     // TODO - better eerrors
     values(tag).asInstanceOf[TypeDescriptorMapping[T]]

object TypeDescriptorRepository
  /** Construct a type descriptor via a hash-map lookup. */
  def apply(lookups: Map[FastTypeTag[?], TypeDescriptorMapping[?]]): TypeDescriptorRepository =
    TypeDescriptorRepositoryImpl(lookups)
  /** 
   * Constructs a TypeDescriptor Repository from a set of types to serialize.
   * 
   * All TypeDescriptorMappings for each type MUST be available on given scope.
   * Additionally, all types must have a valid FastTypeTag.
   */
  inline def apply[T <: Tuple](): TypeDescriptorRepository =
    // note: we could inline the implementation of the find method...
    apply(loadGivenMappings[T])
  import compiletime.{
    erasedValue,
    summonFrom,
    error
  }
  private inline def loadGivenMappings[T <: Tuple]: Map[FastTypeTag[?], TypeDescriptorMapping[?]] =
    inline erasedValue[T] match
      case _: (h *: tail) => 
        Map(loadEntry[h]) ++ loadGivenMappings[tail]
      case _: Unit => Map.empty

  private inline def loadEntry[T]: (FastTypeTag[T], TypeDescriptorMapping[T]) =
    summonFrom {
      case desc: TypeDescriptorMapping[T] => (fastTypeTag[T](), desc)
      // TODO - specific type error w/ T
      case _ => error("Unable to find type descriptor")
    }


/** A type descriptor mapping to use for the raw binary format. 
 * 
 * This simply gives every newly visited field name a new number.
 */
class RawBinaryTypeDescriptorMapping
  extends TypeDescriptorMapping[Any]
  private var lastName: String = null
  private var lastIndex: Int = 0
  def fieldName(num: Int): String = ???
  def fieldNumber(name: String): Int =
    if (name != lastName)
      lastName = name
      lastIndex += 1
    lastIndex
  def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]] =
    Some(RawBinaryTypeDescriptorMapping().asInstanceOf[TypeDescriptorMapping[F]])