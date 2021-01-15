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
  /** The next bit of data is encoded as a Variable-Length-Integer. */
  case VarInt extends WireFormat(0)
  /** The next bit of data is 32-bits in size */
  case Fixed32 extends WireFormat(5)
  /** The next bit of data is 54-bits in size */
  case Fixed64 extends WireFormat(1)
  /** The next bit of data is a VarInt length, followed by that amoount of bytes. */
  case LengthDelimited extends WireFormat(2)
  // Unsupported:
  // - StartGroup, EndGroup

  /** Constructs a proto tag (VarInt value) for the wireformat type and the field number. */
  def makeTag(fieldNumber: Int): Int = (fieldNumber << 3) | flag

object WireFormat:
  private def extractFormat(tag: Int): Int = (tag & 7)
  /** PUll the wire format out of a field tag. */
  def extract(tag: Int): WireFormat =
    extractFormat(tag) match
      case 0 => VarInt
      case 1 => Fixed64
      case 2 => LengthDelimited
      case 5 => Fixed32
      // TODO - good error message

  /** Pulls the field number out of a field tag. */
  def extractField(tag: Int): Int = (tag >>> 3)

/** Construct protocol buffer tag-varints for fields. */
object Tag:
  /** 
   * Constructs a protocol buffer field tag 
   * 
   * @param format The wireformat for the field
   * @param fieldNumber THe number of the field.
   */
  inline def apply(format: WireFormat, fieldNumber: Int): Int = format.makeTag(fieldNumber)
  /**
   * Deconstructs a protocol buffer field tag.
   * 
   * @param tag The field tag value
   * @return a tuple of the WireFormat and field number. 
   */
  inline def unapply(tag: Int): (WireFormat, Int) =
    (WireFormat.extract(tag), WireFormat.extractField(tag))