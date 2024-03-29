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

package sauerkraut.format.pb.streams

import sauerkraut.utils.{
  Endian,
  InlineWriter
}

/** Helper output stream to write protocol buffers. */
final class ProtoOutputStream(out: java.io.OutputStream):
  def writeBoolean(value: Boolean): Unit = InlineWriter.writeBoolean(value, (x) => out.write(x))
  def writeByte(value: Byte): Unit = InlineWriter.writeVarInt32(value.toInt, (x) => out.write(x))
  def writeChar(value: Char): Unit = InlineWriter.writeVarInt32(value.toInt, (x) => out.write(x))
  def writeShort(value: Short): Unit = InlineWriter.writeVarInt32(value.toInt, (x) => out.write(x))
  def writeInt(value: Int): Unit = InlineWriter.writeVarInt32(value, (x) => out.write(x))
  def writeLong(value: Long): Unit = InlineWriter.writeVarInt64(value, (x) => out.write(x))
  def writeFloat(value: Float): Unit = InlineWriter.writeFloat(value, (x) => out.write(x), Endian.Little)
  def writeDouble(value: Double): Unit = InlineWriter.writeDouble(value, (x) => out.write(x), Endian.Little)
  def writeByteArray(bytes: Array[Byte]): Unit =
    writeInt(bytes.length)
    out.write(bytes)
  def writeString(value: String): Unit = writeByteArray(value.getBytes(InlineWriter.Utf8))
  def writeBoolean(field: Int, value: Boolean): Unit = 
    writeInt(WireFormat.VarInt.makeTag(field))
    writeBoolean(value)
  def writeByte(field: Int, value: Byte): Unit = 
    writeInt(WireFormat.VarInt.makeTag(field))
    writeByte(value)
  def writeChar(field: Int, value: Char): Unit = 
    writeInt(WireFormat.VarInt.makeTag(field))
    writeChar(value)
  def writeShort(field: Int, value: Short): Unit =
    writeInt(WireFormat.VarInt.makeTag(field))
    writeShort(value)
  def writeInt(field: Int, value: Int): Unit =
    writeInt(WireFormat.VarInt.makeTag(field))
    writeInt(value)
  def writeLong(field: Int, value: Long): Unit =
    writeInt(WireFormat.VarInt.makeTag(field))
    writeLong(value)
  def writeFloat(field: Int, value: Float): Unit =
    writeInt(WireFormat.Fixed32.makeTag(field))
    writeFloat(value)
  def writeDouble(field: Int, value: Double): Unit =
    writeInt(WireFormat.Fixed64.makeTag(field))
    writeDouble(value)
  def writeByteArray(field: Int, bytes: Array[Byte]): Unit =
    writeInt(WireFormat.LengthDelimited.makeTag(field))
    writeByteArray(bytes)
  def writeString(field: Int, value: String): Unit =
    writeByteArray(field, value.getBytes(InlineWriter.Utf8))

  def flush(): Unit = out.flush()