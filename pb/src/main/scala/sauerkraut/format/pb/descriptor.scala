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
sealed trait ProtoTypeDescriptor[T]:
  /** The type being serialized. */
  def tag: FastTypeTag[T]

/** A descriptor about a protocol buffer message. */
trait MessageProtoDescriptor[T]
    extends ProtoTypeDescriptor[T]:
  /** Lookup the name for a field number. */
  def fieldName(num: Int): String
  /** Lookup the number for a field name. */
  def fieldNumber(name: String): Int
  /** Lookup the field descriptor for a proto. */
  def fieldDesc[F](num: Int): ProtoTypeDescriptor[F]

/** A descriptor for primitives. */
case class PrimitiveTypeDescriptor[T](tag: FastTypeTag[T])
  extends ProtoTypeDescriptor[T]

/** A descriptor for collections. */
case class CollectionTypeDescriptor[Col, T](tag: FastTypeTag[Col], element: ProtoTypeDescriptor[T])
  extends ProtoTypeDescriptor[Col]

object ProtoTypeDescriptor:
  inline def derived[T]: ProtoTypeDescriptor[T] =
    new MessageProtoDescriptor[T] {
      override val tag: FastTypeTag[T] = fastTypeTag[T]()
      private val fieldDescriptors: Array[ProtoTypeDescriptor[?]] =
        summonFieldDescriptors[T]
      override def fieldName(num: Int): String = lookupFieldName[T](num)
      override def fieldNumber(name: String): Int =
        lookupFieldNum[T](name)
      override def fieldDesc[F](num: Int): ProtoTypeDescriptor[F] =
        try fieldDescriptors(fieldNumToIndex[T](num)).asInstanceOf[ProtoTypeDescriptor[F]]
        catch
          case e: java.lang.ArrayIndexOutOfBoundsException =>
            throw RuntimeException(s"Failed with [$tag] to find ${fieldName(num)}($num) at ${fieldNumToIndex[T](num)} in ${fieldDescriptors.mkString("[", ",", "]")}")
    }
  inline def primitive[T]: ProtoTypeDescriptor[T] =
    PrimitiveTypeDescriptor(fastTypeTag[T]())
  inline def collection[Col, T]: ProtoTypeDescriptor[Col] =
    CollectionTypeDescriptor[Col, T](fastTypeTag[Col](), summonFieldDescriptor[T])

  import compiletime.{erasedValue,summonFrom}
  inline private def summonFieldDescriptors[T]: Array[ProtoTypeDescriptor[?]] =
    summonFrom {
      case m: deriving.Mirror.ProductOf[T] =>
        summonFieldDescriptorsImpl[m.MirroredElemTypes]
      case _ => Array()
    }
  // TODO - This should handle all collections...
  import scala.collection.mutable.ArrayBuffer
  inline private def summonFieldDescriptor[T]: ProtoTypeDescriptor[T] =
    inline erasedValue[T] match
        case _: Unit => primitive[T]
        case _: Boolean => primitive[T]
        case _: Byte => primitive[T]
        case _: Char => primitive[T]
        case _: Short => primitive[T]
        case _: Int => primitive[T]
        case _: Long => primitive[T]
        case _: Float => primitive[T]
        case _: Double => primitive[T]
        case _: String => primitive[T]
        case _: List[t] => collection[List[t],t].asInstanceOf[ProtoTypeDescriptor[T]]
        case _: ArrayBuffer[t] => collection[ArrayBuffer[t], t].asInstanceOf[ProtoTypeDescriptor[T]]
        case _ => derived[T]
    
  inline private def summonFieldDescriptorsImpl[T <: Tuple]: Array[ProtoTypeDescriptor[?]] =
    inline erasedValue[T] match
      case _: (field *: rest) =>
        summonFieldDescriptor[field] +: summonFieldDescriptorsImpl[rest]
      case _: Unit => Array()

  // ENTER THE MACROS
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
  // Returns the scala-field-index from the proto field number
  private inline def fieldNumToIndex[T](num: Int): Int =
    summonFrom {
      case m: Mirror.ProductOf[T] =>
        fieldNumToIndexCheck[m.type](num)
    }
  private inline def fieldNumCheck[T](name: String): Int =
    ${fieldNumCheckImpl[T]('name)}
  private inline def fieldNameCheck[T](num: Int): String =
    ${fieldNameCheckImpl[T]('num)}
  private inline def fieldNumToIndexCheck[T](num: Int): Int =
    ${fieldNumToIndexCheckImpl[T]('num)}

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

  def fieldNumToIndexCheckImpl[T](using t: Type[T], qctx: QuoteContext)(id: Expr[Int]): Expr[Int] =
    import qctx.tasty.{_, given _}
    import qctx._
    val helper = MacroHelper(qctx)
    val fieldNamesAndTypesWithNum =
      helper.fieldNamesTypesAndNumber(t.unseal.tpe.asInstanceOf[helper.qctx.tasty.Type])
    val cases: Iterable[CaseDef] =
      fieldNamesAndTypesWithNum.zipWithIndex map {
        case ((label, (tpe, num)), idx) =>
          CaseDef(Literal(Constant(num)), None, Literal(Constant(idx))).asInstanceOf[qctx.tasty.CaseDef]
      }
    Match(id.unseal, cases.toList).seal.asInstanceOf[Expr[Int]] 


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

/**
 * A repository for type descriptors mappings that will be used
 * in serialization/deserialization.
 */ 
trait TypeDescriptorRepository:
  def find[T](tag: FastTypeTag[T]): ProtoTypeDescriptor[T]

private class TypeDescriptorRepositoryImpl(values: Map[FastTypeTag[?], ProtoTypeDescriptor[?]]) 
    extends TypeDescriptorRepository:
  override def find[T](tag: FastTypeTag[T]): ProtoTypeDescriptor[T] =
     // TODO - better eerrors
     values(tag).asInstanceOf[ProtoTypeDescriptor[T]]

object TypeDescriptorRepository:
  /** Construct a type descriptor via a hash-map lookup. */
  def apply(lookups: Map[FastTypeTag[?], ProtoTypeDescriptor[?]]): TypeDescriptorRepository =
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
  private inline def loadGivenMappings[T <: Tuple]: Map[FastTypeTag[?], ProtoTypeDescriptor[?]] =
    inline erasedValue[T] match
      case _: (h *: tail) => 
        Map(loadEntry[h]) ++ loadGivenMappings[tail]
      case _: Unit => Map.empty

  private inline def loadEntry[T]: (FastTypeTag[T], ProtoTypeDescriptor[T]) =
    summonFrom {
      case desc: ProtoTypeDescriptor[T] => (fastTypeTag[T](), desc)
      // TODO - specific type error w/ T
      case _ => error("Unable to find type descriptor")
    }