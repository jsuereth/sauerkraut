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
package json

import core.{PrimitiveBuilder, CollectionBuilder, StructureBuilder}
import org.typelevel.jawn.ast

class JsonReader(value: ast.JValue) extends PickleReader
  def push[T](builder: core.Builder[T]): core.Builder[T] =
    builder match
      case p: PrimitiveBuilder[T] => readPrimitive(p)
      case c: CollectionBuilder[?, T] => readCollection(c)
      case s: StructureBuilder[T] => readStructure(s)
    builder

  def readCollection[E, To](p: CollectionBuilder[E,To]): Unit =
    value match
        case ast.JArray(values) =>
          // TODO - sizeHint
          var idx = 0
          while (idx < values.length)
            JsonReader(values(idx)).push(p.putElement())
            idx += 1
        case _ =>
          // TODO - Allow single values to be treated as an element?
          ()
  
  def readPrimitive[T](p: PrimitiveBuilder[T]): Unit =
    p.tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => p.putPrimitive(value.asBoolean)
      case PrimitiveTag.ByteTag => p.putPrimitive(value.asInt.toByte)
      case PrimitiveTag.CharTag => p.putPrimitive(value.asString(0))
      case PrimitiveTag.ShortTag => p.putPrimitive(value.asInt.toShort)
      case PrimitiveTag.IntTag => p.putPrimitive(value.asInt)
      case PrimitiveTag.LongTag => p.putPrimitive(value.asLong)
      case PrimitiveTag.FloatTag => p.putPrimitive(value.asDouble.toFloat)
      case PrimitiveTag.DoubleTag => p.putPrimitive(value.asDouble)
      case PrimitiveTag.StringTag => p.putPrimitive(value.asString)

  def readStructure[T](p: StructureBuilder[T]): Unit =
    for name <- p.knownFieldNames
    do JsonReader(value.get(name)).push(p.putField(name))