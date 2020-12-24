/*
 * Copyright 2020 Google
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

import com.google.protobuf.{
  CodedInputStream,
  WireFormat
}
import WireFormat.{
  WIRETYPE_LENGTH_DELIMITED
}

/** Helper methods for implementing pb + raw protocols. */
object Shared:
  /** Reads a compressed repeated primitive field. */
  def readCompressedPrimitive[E, To](in: CodedInputStream)(b: core.CollectionBuilder[E, To], elementTag: PrimitiveTag[E]): Unit =
    limitByWireType(in)(WIRETYPE_LENGTH_DELIMITED) {
      while (!in.isAtEnd()) {
        readPrimitive(in)(b.putElement().asInstanceOf)
      }
    }
  /** Reads a primitive by using the Builder's tag to determine how to interpret the data. */
  def readPrimitive[T](in: CodedInputStream)(b: core.PrimitiveBuilder[T]): Unit =
    b.tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => b.putPrimitive(in.readBool())
      case PrimitiveTag.ByteTag => b.putPrimitive(in.readRawByte())
      case PrimitiveTag.CharTag => b.putPrimitive(in.readInt32().toChar)
      case PrimitiveTag.ShortTag => b.putPrimitive(in.readInt32().toShort)
      case PrimitiveTag.IntTag => b.putPrimitive(in.readInt32())
      case PrimitiveTag.LongTag => b.putPrimitive(in.readInt64())
      case PrimitiveTag.FloatTag => b.putPrimitive(in.readFloat())
      case PrimitiveTag.DoubleTag => b.putPrimitive(in.readDouble())
      case PrimitiveTag.StringTag => b.putPrimitive(in.readString())

  inline def limitByWireType[A](in: CodedInputStream)(wireType: Int)(f: => A): Unit =
    // TODO - if field is a STRING we do not limit by length.
    if wireType == WIRETYPE_LENGTH_DELIMITED
    then
      var length = in.readRawVarint32()
      val limit = in.pushLimit(length)
      f
      in.popLimit(limit)
    else f