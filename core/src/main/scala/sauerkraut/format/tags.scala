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
enum PrimitiveTag[T] extends FastTypeTag[T]
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
enum NonPrimitiveTag[T] extends FastTypeTag[T]
  /** A type representing a structure of key-value pairs. */
  case Struct[T](name: String) extends NonPrimitiveTag[T]
  /** A non-primitive type, where the value could be one of several options. */
  case Option[T](name: String, options: List[FastTypeTag[?]]) extends NonPrimitiveTag[T]
  /** A Reader + Writer are available in given scope for this type. */
  case Given[T](name: String) extends NonPrimitiveTag[T]

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
        // TODO - file bug for this not working
        // case _: Unit | Boolean | Char | Short | Int | Long | Float | Double | String => primitiveTag[T]()
        case _ => compiletime.summonFrom {
          // TODO - not all products + sums are supported...
          case m: deriving.Mirror.ProductOf[T] => NonPrimitiveTag.Struct[T](typeName[T])
          case m: deriving.Mirror.SumOf[T] =>
            // TODO - figure out everything we'll actually need here...
            NonPrimitiveTag.Option[T](typeName[T], options[m.MirroredElemTypes])
          case p: core.Pickler[T] => NonPrimitiveTag.Given[T](typeName[T])
          case _ =>  unsupportedType[T]
        }
/** From a tuple of types, pulls all the type tags. */
inline def options[Elems]: List[FastTypeTag[?]] =
  inline erasedValue[Elems] match
    case _: (h *: tail) => fastTypeTag[h]() :: options[tail]
    case _: Unit => Nil
import scala.quoted._
private def typeNameImpl[T: Type](using QuoteContext): Expr[String] =
  Expr(summon[Type[T]].show)
/** Pulls a full-string (unique) name for the given type. */
inline def typeName[T]: String = ${typeNameImpl[T]}

private inline def unsupportedType[T]: FastTypeTag[T] = ${unsupportedTypeImpl[T]}
private def unsupportedTypeImpl[T: Type](using qctx: QuoteContext): Expr[FastTypeTag[T]] =
  qctx.error(s"Unsupported saurekraut pickling type: ${summon[Type[T]].show}")
  Expr(null)
private inline def notPrimitiveError[T]: PrimitiveTag[T] = ${notPrimitiveErrorImpl[T]}
private def notPrimitiveErrorImpl[T: Type](using qctx: QuoteContext): Expr[PrimitiveTag[T]] =
  qctx.error(s"Not a sauerkraut primitive type: ${summon[Type[T]].show}")
  Expr(null)