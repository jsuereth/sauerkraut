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

package sauerkraut.format

enum FastTypeTag[T]
  case UnitTag extends FastTypeTag[Unit]
  case BooleanTag extends FastTypeTag[Boolean]
  case CharTag extends FastTypeTag[Char]
  case ShortTag extends FastTypeTag[Short]
  case IntTag extends FastTypeTag[Int]
  case LongTag extends FastTypeTag[Long]
  case FloatTag extends FastTypeTag[Float]
  case DoubleTag extends FastTypeTag[Double]
  case StringTag extends FastTypeTag[String]
  // TODO - case ArrayByte extends FastTypeTag[Array[Byte]]
  // TODO - determine the right mechanism to refrence non-primitive types.
  /** A non-primitive type, where we keep the fully-qualified name. */
  case Named[T](name: String) extends FastTypeTag[T]


import compiletime.erasedValue
inline def fastTypeTag[T](): FastTypeTag[T] =
    inline erasedValue[T] match
        case _: Unit => FastTypeTag.UnitTag.asInstanceOf
        case _: Boolean => FastTypeTag.BooleanTag.asInstanceOf
        case _: Char => FastTypeTag.CharTag.asInstanceOf
        case _: Short => FastTypeTag.ShortTag.asInstanceOf
        case _: Int => FastTypeTag.IntTag.asInstanceOf
        case _: Long => FastTypeTag.LongTag.asInstanceOf
        case _: Float => FastTypeTag.FloatTag.asInstanceOf
        case _: Double => FastTypeTag.DoubleTag.asInstanceOf
        case _: String => FastTypeTag.StringTag.asInstanceOf
        case _ => compiletime.summonFrom {
          case m: deriving.Mirror.ProductOf[T] => FastTypeTag.Named[T](typeName[T])
          case _ => compiletime.error("Unsupported type!")
        }

import scala.quoted._
private def typeNameImpl[T: Type](using QuoteContext): Expr[String] =
  Expr(summon[Type[T]].show)
/** Pulls a full-string (unique) name for the given type. */
inline def typeName[T]: String = ${typeNameImpl[T]}