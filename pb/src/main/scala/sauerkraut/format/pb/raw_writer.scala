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

import com.google.protobuf.{CodedOutputStream,WireFormat}

/**
 * A PickleWriter that writes protocol-buffer-like pickles.   This will NOT
 * lookup appropriate field numbers per type, but instead number fields in order
 * it sees them as 1->N. This is ok for ephemeral serialization where there is no
 * class/definition skew, but not ok in most serialization applications.
 */
class RawBinaryPickleWriter(out: CodedOutputStream) extends PickleWriter with PickleCollectionWriter
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    // When writing 'raw' collections, we just write a length, then each element.
    out.writeInt32NoTag(length)
    work(this)
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    work(RawBinaryStructureWriter(out))
    this
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => out.writeBoolNoTag(picklee.asInstanceOf[Boolean])
      case PrimitiveTag.ByteTag => out.write(picklee.asInstanceOf[Byte])
      case PrimitiveTag.CharTag => out.writeInt32NoTag(picklee.asInstanceOf[Char].toInt)
      case PrimitiveTag.ShortTag => out.writeInt32NoTag(picklee.asInstanceOf[Short].toInt)
      case PrimitiveTag.IntTag => out.writeInt32NoTag(picklee.asInstanceOf[Int])
      case PrimitiveTag.LongTag => out.writeInt64NoTag(picklee.asInstanceOf[Long])
      case PrimitiveTag.FloatTag => out.writeFloatNoTag(picklee.asInstanceOf[Float])
      case PrimitiveTag.DoubleTag => out.writeDoubleNoTag(picklee.asInstanceOf[Double])
      case PrimitiveTag.StringTag => out.writeStringNoTag(picklee.asInstanceOf[String])
    this
  override def flush(): Unit = out.flush()

/** 
 * An unknown protocol buffer structure writer.  It simply gives all new fields
 * a new index, starting with 1 and moving up.
 */
class RawBinaryStructureWriter(out: CodedOutputStream) extends PickleStructureWriter
  private var currentFieldIndex = 0
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    currentFieldIndex += 1
    pickler(RawBinaryFieldWriter(out, currentFieldIndex))
    this


class RawBinaryFieldWriter(out: CodedOutputStream, fieldNum: Int) 
    extends PickleWriter
  // Writing a collection should simple write a field multiple times.
  // TODO - see if we can determine type and use the alternative encoding.
  override def putCollection(length: Int)(work : PickleCollectionWriter => Unit): PickleWriter =
    // Collections are written as:
    // [TAG] [LengthInBytes] [LengthOfCollection] [Element]*
    val sizeEstimate = RawCollectionSizeEstimateWriter()
    work(sizeEstimate)
    out.writeTag(fieldNum, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    out.writeInt32NoTag(
      CodedOutputStream.computeInt32SizeNoTag(length) +
      sizeEstimate.finalSize)
    out.writeInt32NoTag(length)
    work(RawBinaryPickleWriter(out))
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    val sizeEstimate = FieldSizeEstimateWriter(fieldNum, None)
    sizeEstimate.putStructure(picklee, tag)(work)
    out.writeTag(fieldNum, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    work(RawBinaryStructureWriter(out))
    this

  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => out.writeBool(fieldNum, picklee.asInstanceOf[Boolean])
      case PrimitiveTag.CharTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Char].toInt)
      case PrimitiveTag.ShortTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Short].toInt)
      case PrimitiveTag.IntTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Int])
      case PrimitiveTag.LongTag => out.writeInt64(fieldNum, picklee.asInstanceOf[Long])
      case PrimitiveTag.FloatTag => out.writeFloat(fieldNum, picklee.asInstanceOf[Float])
      case PrimitiveTag.DoubleTag => out.writeDouble(fieldNum, picklee.asInstanceOf[Double])
      case PrimitiveTag.StringTag => out.writeString(fieldNum, picklee.asInstanceOf[String])
    this

  override def flush(): Unit = out.flush()
