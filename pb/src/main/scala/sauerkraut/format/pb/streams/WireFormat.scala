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

/** Wire format tags + flags for Protocol buffer encoding. */
enum WireFormat(val flag: Int):
  case VarInt extends WireFormat(0)
  case Fixed32 extends WireFormat(5)
  case Fixed64 extends WireFormat(1)
  case LengthDelimited extends WireFormat(2)
  // Unsupported:
  // - StartGroup, EndGroup

  // Constructs a proto tag for the wireformat type and the field number.
  def makeTag(fieldNumber: Int): Int = (fieldNumber << 3) | flag

object WireFormat:
  def extractFormat(tag: Int): Int = (tag & 7)
  def extract(tag: Int): WireFormat =
    extractFormat(tag) match
      case 0 => VarInt
      case 1 => Fixed64
      case 2 => LengthDelimited
      case 5 => Fixed32
      // TODO - good error message
  def extractField(tag: Int): Int = (tag >>> 3)

/** Construct protocol buffer tag-varints for fields. */
object Tag:
  inline def apply(format: WireFormat, fieldNumber: Int): Int = format.makeTag(fieldNumber)
  inline def unapply(tag: Int): (WireFormat, Int) =
    (WireFormat.extract(tag), WireFormat.extractField(tag))