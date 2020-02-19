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

import com.google.protobuf.{
  CodedInputStream,
  WireFormat
}
import WireFormat.{
  WIRETYPE_LENGTH_DELIMITED
}

object Tag
  def unapply(tag: Int): (Int, Int) =
    (WireFormat.getTagWireType(tag), WireFormat.getTagFieldNumber(tag))

class RawBinaryPickleReader(in: CodedInputStream)
  extends PickleReader
  override def push[T](b: core.Builder[T]): core.Builder[T] =
    b match
      case p: core.PrimitiveBuilder[T] => readPrimitive(p)
      case c: core.CollectionBuilder[_, T] => readCollection(c)
      case s: core.StructureBuilder[T] => readStructure(s)
    b

  private def readPrimitive[T](b: core.PrimitiveBuilder[T]): Unit =
    b.tag match
        case PrimitiveTag.UnitTag => ()
        case PrimitiveTag.BooleanTag => b.putPrimitive(in.readBool())
        case PrimitiveTag.ByteTag => b.putPrimitive(in.readRawByte())
        case PrimitiveTag.CharTag => b.putPrimitive(in.readInt32().toChar)
        case PrimitiveTag.ShortTag => b.putPrimitive(in.readInt32().toShort)
        case PrimitiveTag.IntTag => b.putPrimitive(in.readInt32())
        case PrimitiveTag.LongTag => b.putPrimitive(in.readInt64())
        case PrimitiveTag.FloatTag => b.putPrimitive(in.readFloat())
        case PrimitiveTag.DoubleTag => b.putPrimitive(in.readDouble())
        case PrimitiveTag.StringTag => b.putPrimitive(in.readString())
  private def readStructure[T](struct: core.StructureBuilder[T]): Unit =
    object Field
      def unapply(num: Int): Option[core.Builder[?]] =
        if (num > 0 && num <= struct.knownFieldNames.length)
          Some(struct.putField(struct.knownFieldNames(num-1)))
        else None
    var done: Boolean = false
    while (!done)
      in.readTag match
        // TODO - if we hit any field we don't recognize, we quit.
        case 0 => done = true
        // Special case string (and packed) types so we don't read the length.
        case Tag(WIRETYPE_LENGTH_DELIMITED,
                 fieldNum @ Field(fieldBuilder: core.PrimitiveBuilder[?])) =>
          // Don't read the length, string read will do this.
          RawBinaryPickleReader(in).push(fieldBuilder)
        // All other types are somewhat uniform.
        case Tag(wireType, fieldNum @ Field(fieldBuilder)) =>
          limitByWireType(wireType) {
            // For repeating fields, we just add to a collection.
            fieldBuilder match {
              case x: core.CollectionBuilder[?, ?] =>
                RawBinaryPickleReader(in).push(x.putElement())
              case y => 
                RawBinaryPickleReader(in).push(y)
            }
          }
        case _ => done = true
  inline private def limitByWireType[A](wireType: Int)(f: => A): Unit =
    if (wireType == WIRETYPE_LENGTH_DELIMITED)
      var length = in.readRawVarint32()
      val limit = in.pushLimit(length)
      f
      in.popLimit(limit)
    else f
  private def readCollection[E, To](c: core.CollectionBuilder[E, To]): Unit  =
    // Collections are written as:
    // [TAG] [LengthInBytes] [LengthOfCollection] [Element]*
    var length = in.readRawVarint32()
    // TODO - sizeHint
    while (length > 0)
      RawBinaryPickleReader(in).push(c.putElement())
      length -= 1
