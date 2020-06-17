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

package sauerkraut.format.nbt.internal

// See: https://minecraft.gamepedia.com/NBT_format
enum NbtTag(val id: Int):
  case TagEnd extends NbtTag(0)
  case TagByte extends NbtTag(1) 
  case TagShort extends NbtTag(2)
  case TagInt extends NbtTag(3)
  case TagLong extends NbtTag(4)
  case TagFloat extends NbtTag(5)
  case TagDouble extends NbtTag(6)
  case TagByteArray extends NbtTag(7) 
  case TagString extends NbtTag(8)
  case TagList extends NbtTag(9)
  case TagCompound extends NbtTag(10)
  case TagIntArray extends NbtTag(11)
  case TagLongArray extends NbtTag(12)

object NbtTag:
  def fromByte(b: Byte): NbtTag =
    b match
      case 0 => TagEnd
      case 1 => TagByte
      case 2 => TagShort
      case 3 => TagInt
      case 4 => TagLong
      case 5 => TagFloat
      case 6 => TagDouble
      case 7 => TagByteArray
      case 8 => TagString
      case 9 => TagList
      case 10 => TagCompound
      case 11 => TagIntArray
      case 12 => TagLongArray
      case _ => TagEnd // TODO - error?
    