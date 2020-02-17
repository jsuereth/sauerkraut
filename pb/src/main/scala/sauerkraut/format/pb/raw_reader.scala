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

class RawBinaryPickleReader(in: CodedInputStream)
  extends PickleReader
  override def readPrimitive[T](tag: PrimitiveTag[T]): T =
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
    reader(RawBinaryStructureReader(in))
  override def readCollection[E, To](
    builder: Builder[E, To],
    elementReader: PickleReader => E): To =
    var length = in.readInt32()
    builder.sizeHint(length)
    while (length > 0)
      builder += elementReader(this)
      length -= 1
    builder.result 


class RawBinaryStructureReader(in: CodedInputStream)
  extends StructureReader
  private var latestCount = 0
  private var latestName: String = null
  // TODO - remember fields we've seen.
  override def readField[T](name: String, fieldReader: PickleReader => T): T =
    if (latestName != name)
      latestName = name
      latestCount += 1
    // TODO - if this is a collection, we need to ignore the
    // tag on each element, not here...
    val tag = in.readTag()
    // TODO - figure out what to assert here...
    //assert(latestCount == tag, s"Expected $latestCount, but found $tag")
    fieldReader(RawBinaryPickleReader(in))


