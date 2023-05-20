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

package sauerkraut.format.pb.streams

import sauerkraut.utils.{
  VarInt,
  InlineWriter
}

/**
 * Helper utilities to calculate protocol buffer wire size for types.
 */
object ProtoWireSize:
  inline def sizeOf(value: Boolean): Int = 1
  inline def sizeOf(value: Byte): Int = 1
  inline def sizeOf(value: Int): Int =  VarInt.byteSize(value)
  inline def sizeOf(value: Long): Int =  VarInt.byteSize(value)
  inline def sizeOf(value: Float): Int =  4
  inline def sizeOf(value: Double): Int =  8
  inline def sizeOf(value: Array[Byte]): Int =
    sizeOf(value.length) + value.length
  inline def sizeOf(value: String): Int = 
    // TODO - Faster impl?
    val length = value.getBytes(InlineWriter.Utf8).length
    sizeOf(length) + length
  inline def sizeOfTag(format: WireFormat, fieldNumber: Int): Int = sizeOf(format.makeTag(fieldNumber))


  inline def sizeOf(field: Int, value: Boolean): Int = sizeOfTag(WireFormat.VarInt, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Byte): Int = sizeOfTag(WireFormat.VarInt, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Int): Int = sizeOfTag(WireFormat.VarInt, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Long): Int = sizeOfTag(WireFormat.VarInt, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Float): Int = sizeOfTag(WireFormat.Fixed32, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Double): Int = sizeOfTag(WireFormat.Fixed64, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: String): Int = sizeOfTag(WireFormat.LengthDelimited, field) + sizeOf(value)
  inline def sizeOf(field: Int, value: Array[Byte]): Int = sizeOfTag(WireFormat.LengthDelimited, field) + sizeOf(value)
