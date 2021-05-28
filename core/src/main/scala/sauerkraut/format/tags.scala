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

import core.internal.InlineHelper

/** 
 * A hierarchy on types that represent how they are pickled/unpickled.
 * 
 * - FastTypeTag
 *   - PrimitiveTag
 *     - UnitTag
 *     - BooleanTag
 *     - CharTag
 *     - ShortTag
 *     - IntTag
 *     - LongTag
 *     - FloatTag
 *     - DoubleTag
 *     - StringTag
 *   - Named
 */ 
sealed trait FastTypeTag[T]
object FastTypeTag
enum PrimitiveTag[T] extends FastTypeTag[T]:
  case UnitTag extends PrimitiveTag[Unit]
  case BooleanTag extends PrimitiveTag[Boolean]
  case ByteTag extends PrimitiveTag[Byte]
  case CharTag extends PrimitiveTag[Char]
  case ShortTag extends PrimitiveTag[Short]
  case IntTag extends PrimitiveTag[Int]
  case LongTag extends PrimitiveTag[Long]
  case FloatTag extends PrimitiveTag[Float]
  case DoubleTag extends PrimitiveTag[Double]
  case StringTag extends PrimitiveTag[String]
// TODO - case ArrayByteTag extends FastTypeTag[Array[Byte]]

// TODO - determine the right mechanism to refrence non-primitive types.
sealed trait NonPrimitiveTag[T] extends FastTypeTag[T]
/** A type representing a structure of key-value pairs. */
sealed trait Struct[T] extends NonPrimitiveTag[T]:
  def name: String
  def fields: Array[String]
  final override def hashCode: Int = name.hashCode
  final override def equals(other: Any): Boolean =
    other match
      case o: Struct[_] => o.name == name
      case _ => false
  final override def toString = s"Struct($name)"

private class CustomStruct[T](
  override val name: String,
  override val fields: Array[String]
) extends Struct[T]
/** Constructs a custom structure-definition tag. */
inline def structTag[T](fields: Array[String]): Struct[T] =
  CustomStruct(typeName[T], fields)

/** A non-primitive type, where the value could be one of several options. */
sealed trait Choice[T] extends NonPrimitiveTag[T]:
  def name: String
  def options: List[FastTypeTag[?]]
  def find(name: String): FastTypeTag[?]
  def ordinal[T](value: T): Int
  def nameFromOrdinal(ordinal: Int): String
  final override def hashCode: Int = name.hashCode
  final override def equals(other: Any): Boolean = 
    other match
      case o: Choice[_] => o.name == name
      case _ => false
  final override def toString = s"Choice($name)"

// TODO - implement this.
inline def choiceTag[T](/* options */): Choice[T] = ???

/** A collection type, where we can draw out its element type. */
final class CollectionTag[T, E](
  val name: String,
  val elementTag: FastTypeTag[E]
) extends NonPrimitiveTag[T]:
  final override def hashCode: Int = name.hashCode + 13*elementTag.hashCode
  final override def equals(other: Any): Boolean =
    other match
      case o: CollectionTag[_, _] => (o.name == name) && (o.elementTag == elementTag)
      case _ => false
  final override def toString = s"Collection($name)"

/** 
 * Constructs a ccollection tag for collections of type T containing repeated
 * elements of type E. 
 */
inline def collectionTag[T : reflect.ClassTag, E](elementTag: FastTypeTag[E]): CollectionTag[T,E] =
  // TODO - use a macro attempt to grab type constructor or some other
  // mechanism to get an idempotent AND unique name for collection + element.
  CollectionTag(summon[reflect.ClassTag[T]].runtimeClass.getName, elementTag)

/** Constructs a primitive tag or throws a compiletime error. */
import compiletime.erasedValue
inline def primitiveTag[T](): PrimitiveTag[T] =
    inline erasedValue[T] match
        case _: Unit => PrimitiveTag.UnitTag.asInstanceOf[PrimitiveTag[T]]
        case _: Boolean => PrimitiveTag.BooleanTag.asInstanceOf[PrimitiveTag[T]]
        case _: Byte => PrimitiveTag.ByteTag.asInstanceOf[PrimitiveTag[T]]
        case _: Char => PrimitiveTag.CharTag.asInstanceOf[PrimitiveTag[T]]
        case _: Short => PrimitiveTag.ShortTag.asInstanceOf[PrimitiveTag[T]]
        case _: Int => PrimitiveTag.IntTag.asInstanceOf[PrimitiveTag[T]]
        case _: Long => PrimitiveTag.LongTag.asInstanceOf[PrimitiveTag[T]]
        case _: Float => PrimitiveTag.FloatTag.asInstanceOf[PrimitiveTag[T]]
        case _: Double => PrimitiveTag.DoubleTag.asInstanceOf[PrimitiveTag[T]]
        case _: String => PrimitiveTag.StringTag.asInstanceOf[PrimitiveTag[T]]
        case _ => notPrimitiveError[T]

/**
 * Synthesizes reflection information for type T.
 * 
 * This method will attempt to generate a "FastTypeTag" for the type T.
 * 
 * - If T is a primitive, you get a PrimitiveTag
 * - If T is derivable (Struct/Choice) you will get a synthesized tag.
 * - If T has an implicitly available `Pickler[T]`, then its tag is returned. 
 * 
 * This lookup is done in the specified order, allowing the usage of `fastTypeTag`
 * when deriving an implicitly available `Pickler[T]`, breaking any looped lookup.
 * 
 * If you are synthesizing your own `Pickler[T]` by hand, please use one of:
 * - [[collectionTag]]
 * - [[choiceTag]]
 * - [[structTag]]
 */
inline def fastTypeTag[T](): FastTypeTag[T] =
    inline erasedValue[T] match
        case _: Unit => primitiveTag[T]()
        case _: Boolean => primitiveTag[T]()
        case _: Byte => primitiveTag[T]()
        case _: Char => primitiveTag[T]()
        case _: Short => primitiveTag[T]()
        case _: Int => primitiveTag[T]()
        case _: Long => primitiveTag[T]()
        case _: Float => primitiveTag[T]()
        case _: Double => primitiveTag[T]()
        case _: String => primitiveTag[T]()
        case _ => compiletime.summonFrom {
          // TODO: Createa a mechanism whereby we can override "bad" deriviations for
          // collections (e.g. List)
          case m: deriving.Mirror.ProductOf[T] =>
            new Struct[T] {
              override val name = typeName[T]
              override val fields = InlineHelper.summonLabels[m.MirroredElemLabels].toArray
            }
          case m: deriving.Mirror.SumOf[T] =>
            new Choice[T] {
              override val name = typeName[T]
              override def ordinal[T](value: T): Int = m.ordinal(value.asInstanceOf[m.MirroredMonoType])
              override val options = format.options[m.MirroredElemTypes]
              override def nameFromOrdinal(ordinal: Int): String =
                InlineHelper.labelLookup[m.MirroredElemLabels](ordinal)
              override def find(name: String): FastTypeTag[?] = 
                choiceMatcher[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]](name)
            }
          // If the user has given us a pickler, use the reflection they provide.
          case p: core.Pickler[T] => p.tag
          case _ =>  unsupportedType[T]
        }

inline def choiceMatcher[NamesAndElems <: Tuple](name: String): FastTypeTag[?] =
  inline erasedValue[NamesAndElems] match
    case _: (Tuple2[name, tpe] *: tail) =>
      if name == compiletime.constValue[name]
      then fastTypeTag[tpe]()
      else choiceMatcher[tail](name)
    // TODO - better errors.
    case _: EmptyTuple => throw RuntimeException(s"Could not find $name")

/** From a tuple of types, pulls all the type tags. */
inline def options[Elems]: List[FastTypeTag[?]] =
  inline erasedValue[Elems] match
    case _: (h *: tail) => fastTypeTag[h]() :: options[tail]
    case _: EmptyTuple  => Nil
import scala.quoted._
private def typeNameImpl[T: Type](using Quotes): Expr[String] =
  Expr(Type.show[T])
/** Pulls a full-string (unique) name for the given type. */
inline def typeName[T]: String = ${typeNameImpl[T]}

private inline def unsupportedType[T]: FastTypeTag[T] = ${unsupportedTypeImpl[T]}
private def unsupportedTypeImpl[T: Type](using qctx: Quotes): Expr[FastTypeTag[T]] =
  quotes.reflect.report.error(s"Unsupported saurekraut pickling type: ${Type.show[T]}")
  '{???}
private inline def notPrimitiveError[T]: PrimitiveTag[T] = ${notPrimitiveErrorImpl[T]}
private def notPrimitiveErrorImpl[T: Type](using qctx: Quotes): Expr[PrimitiveTag[T]] =
  quotes.reflect.report.error(s"Not a sauerkraut primitive type: ${Type.show[T]}")
  '{???}