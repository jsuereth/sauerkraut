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

import core.{
  PrimitiveBuilder,
  CollectionBuilder,
  StructureBuilder,
  ChoiceBuilder
}
import org.typelevel.jawn.ast

class JsonReader(value: ast.JValue) extends PickleReader:
  def push[T](builder: core.Builder[T]): core.Builder[T] =
    builder match
      case p: PrimitiveBuilder[T] => readPrimitive(p)
      case c: CollectionBuilder[?, T] => readCollection(c)
      case s: StructureBuilder[T] => readStructure(s)
      case c: ChoiceBuilder[T] => readChoice(c)
    builder

  def readCollection[E, To](p: CollectionBuilder[E,To]): Unit =
    value match
        case ast.JArray(values) =>
          p.sizeHint(values.length)
          var idx = 0
          while (idx < values.length)
            JsonReader(values(idx)).push(p.putElement())
            idx += 1
        // We attempt to push a single element into the collection, assuming
        // the user has migrated the previous version from T => Collection[T].
        case other => push(p.putElement())
  
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
    for name <- p.tag.fields
    do JsonReader(value.get(name)).push(p.putField(name))

  def readChoice[T](p: ChoiceBuilder[T]): Unit =
    value match {
      case ast.JObject(values) if !values.isEmpty =>
        val (key,value) = values.head
        JsonReader(value).push(p.putChoice(key))
      // TODO - better error messages!
      case _ => // Ignore.
    }