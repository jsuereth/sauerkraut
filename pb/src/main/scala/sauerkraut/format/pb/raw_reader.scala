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

class RawBinaryPickleReader(in: CodedInputStream)
  extends PickleReader
  override def readPrimitive[T](tag: FastTypeTag[T]): T =
    tag match {
        case FastTypeTag.UnitTag => ()
        case FastTypeTag.BooleanTag => in.readBool()
        case FastTypeTag.CharTag => in.readInt32().toChar
        case FastTypeTag.ShortTag => in.readInt32().toShort
        case FastTypeTag.IntTag => in.readInt32()
        case FastTypeTag.LongTag => in.readInt64()
        case FastTypeTag.FloatTag => in.readFloat()
        case FastTypeTag.DoubleTag => in.readDouble()
        case FastTypeTag.StringTag => in.readString()
        case _ => ???
    }
  override def readStructure[T](reader: StructureReader => T): T =
    reader(RawBinaryStructureReader(in))

class RawBinaryStructureReader(in: CodedInputStream)
  extends StructureReader
  private var latestCount = 0
  private var latestName: String = null
  // TODO - remember fields we've seen.
  override def readField[T](name: String, fieldReader: PickleReader => T): T =
    if (latestName != name)
      latestName = name
      latestCount += 1
    val tag = in.readTag()
    // TODO - figure out what to assert here...
    //assert(latestCount == tag, s"Expected $latestCount, but found $tag")
    fieldReader(RawBinaryPickleReader(in))


