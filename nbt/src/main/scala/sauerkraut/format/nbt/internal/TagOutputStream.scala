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
package nbt
package internal

import java.io.OutputStream
import sauerkraut.utils.InlineWriter
import InlineWriter.Endian
import NbtTag._

/** Helper class to output Tags + Payloads for NBT format. */
class TagOutputStream(out: OutputStream):
  /** Writes a raw tag-id, with no paylod. */
  def writeRawTag(tag: NbtTag): Unit = out.write(tag.id)
  def writeStringPayload(value: String): Unit = 
    // We likely want to check and make sure this is really how NBT does it..
    val bytes = value.getBytes(InlineWriter.Utf8)    
    writeShortPayload(bytes.length.toShort)
    out.write(bytes)
    //out.writeUTF(value)
  def writeBytePayload(value: Byte): Unit =
    out.write(value.toInt)
  def writeShortPayload(value: Short): Unit =
    InlineWriter.writeFixed16(value, (x) => out.write(x), Endian.Big)
    // out.writeShort(value.toInt)
  def writeIntPayload(value: Int): Unit =
    InlineWriter.writeFixed32(value, (x) => out.write(x), Endian.Big)
    // out.writeInt(value)
  def writeLongPayload(value: Long): Unit =
    InlineWriter.writeFixed64(value, (x) => out.write(x), Endian.Big)
    // out.writeLong(value)
  def writeFloatPayload(value: Float): Unit =
    InlineWriter.writeFloat(value, (x) => out.write(x), Endian.Big)
    // out.writeFloat(value)
  def writeDoublePayload(value: Double): Unit =
    InlineWriter.writeDouble(value, (x) => out.write(x), Endian.Big)
    // out.writeDouble(value)
  def writeBytesPayload(value: Array[Byte]): Unit =
    writeIntPayload(value.length)
    out.write(value)
  def writeIntArrayPayload(value: Array[Int]): Unit =
    writeIntPayload(value.length)
    value.foreach(writeIntPayload)
  def writeLongArrayPayload(value: Array[Long]): Unit =
    writeIntPayload(value.length)
    value.foreach(writeLongPayload)
  def flush(): Unit = out.flush()
  def close(): Unit = out.close()


