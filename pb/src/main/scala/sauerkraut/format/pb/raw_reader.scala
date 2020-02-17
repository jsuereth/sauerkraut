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

import com.google.protobuf.CodedInputStream
import scala.collection.mutable.Builder

class RawBinaryPickleReader(in: CodedInputStream, root: Boolean = true)
  extends PickleReader
  override def readPrimitive[T](tag: PrimitiveTag[T]): T =
    if (!root) in.readTag() // Ignore for now...
    tag match
        case PrimitiveTag.UnitTag => ()
        case PrimitiveTag.BooleanTag => in.readBool()
        case PrimitiveTag.CharTag => in.readInt32().toChar
        case PrimitiveTag.ShortTag => in.readInt32().toShort
        case PrimitiveTag.IntTag => in.readInt32()
        case PrimitiveTag.LongTag => in.readInt64()
        case PrimitiveTag.FloatTag => in.readFloat()
        case PrimitiveTag.DoubleTag => in.readDouble()
        case PrimitiveTag.StringTag => in.readString()
  override def readStructure[T](reader: StructureReader => T): T =
    if (!root)
      // Structures have a tag
      val tag = in.readTag()
      // Structures have a size.  TODO - only read up to that size?
      val size = in.readRawVarint32()
    reader(RawBinaryStructureReader(in))
  override def readCollection[E, To](
    builder: Builder[E, To],
    elementReader: PickleReader => E): To =
    if (root)
      var length = in.readRawVarint32()
      builder.sizeHint(length)
      while (length > 0)
        builder += elementReader(this)
        length -= 1
      builder.result
    else
      Console.err.println(s"\n\n-DEBUG- Reading collection w/ field tags...\n\n")
      builder.result


class RawBinaryStructureReader(in: CodedInputStream)
  extends StructureReader
  private var latestCount = 0
  private var latestName: String = null
  // TODO - remember fields we've seen.
  override def readField[T](name: String, fieldReader: PickleReader => T): T =
    // TODO - remember the field?
    if (latestName != name)
      latestName = name
      latestCount += 1
    fieldReader(RawBinaryPickleReader(in, root = false))


