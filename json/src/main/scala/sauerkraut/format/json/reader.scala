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


import org.typelevel.jawn.ast

class JsonReader(value: ast.JValue) extends PickleReader
  override def readCollection[E, To](
      builder: scala.collection.mutable.Builder[E, To],
      elementReader: sauerkraut.format.PickleReader => E): To =
      value match
        case ast.JArray(values) =>
          builder.sizeHint(values.length)
          var idx = 0
          while (idx < values.length)
            builder += elementReader(JsonReader(values(idx)))
            idx += 1
        case _ =>
          // TODO - error?
      builder.result
  override def readPrimitive[T](
      tag: sauerkraut.format.PrimitiveTag[T]): T =
        tag match
          case PrimitiveTag.UnitTag => ()
          case PrimitiveTag.BooleanTag => value.asBoolean
          case PrimitiveTag.ByteTag => value.asInt.toByte
          case PrimitiveTag.CharTag => value.asString(0)
          case PrimitiveTag.ShortTag => value.asInt.toShort
          case PrimitiveTag.IntTag => value.asInt
          case PrimitiveTag.LongTag => value.asLong
          case PrimitiveTag.FloatTag => value.asDouble.toFloat
          case PrimitiveTag.DoubleTag => value.asDouble
          case PrimitiveTag.StringTag => value.asString
  override def readStructure[T](
      reader: sauerkraut.format.StructureReader => T): T =
      reader(JsonStructureReader(value))

class JsonStructureReader(value: ast.JValue) extends StructureReader
  override def readField[T](name: String, fieldReader: PickleReader => T): T =
    fieldReader(JsonReader(value.get(name)))