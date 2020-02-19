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

package sauerkraut.format.nbt
package internal

import java.io.DataOutputStream
import NbtTag._

class TagOutputStream(out: DataOutputStream) {
    /** Writes a raw tag-id, with no paylod. */
    def writeRawTag(tag: NbtTag): Unit =
      out.writeByte(tag.id)
    def writeStringPayload(value: String): Unit =
      out.writeUTF(value)
    def writeBytePayload(value: Byte): Unit =
      out.writeByte(value.toInt)
    def writeShortPayload(value: Short): Unit =
      out.writeShort(value.toInt)
    def writeIntPayload(value: Int): Unit =
      out.writeInt(value)
    def writeLongPayload(value: Long): Unit =
      out.writeLong(value)
    def writeFloatPayload(value: Float): Unit =
      out.writeFloat(value)
    def writeDoublePayload(value: Double): Unit =
      out.writeDouble(value)
    def writeBytesPayload(value: Array[Byte]): Unit =
      out.writeInt(value.length)
      out.write(value)
    def writeIntArrayPayload(value: Array[Int]): Unit =
      out.writeInt(value.length)
      value.foreach(out.writeInt)
    def writeLongArrayPayload(value: Array[Long]): Unit =
      out.writeInt(value.length)
      value.foreach(out.writeLong)

    def flush(): Unit = out.flush()
    def close(): Unit = out.close()
}

