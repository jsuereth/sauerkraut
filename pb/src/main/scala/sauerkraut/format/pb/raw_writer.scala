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

import streams.{ProtoOutputStream, WireFormat}

/**
 * A PickleWriter that writes protocol-buffer-like pickles.   This will NOT
 * lookup appropriate field numbers per type, but instead number fields in order
 * it sees them as 1->N. This is ok for ephemeral serialization where there is no
 * class/definition skew, but not ok in most serialization applications.
 */
class RawBinaryPickleWriter(out: ProtoOutputStream) extends PickleWriter with PickleCollectionWriter:
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    // When writing 'raw' collections, we just write a length, then each element.
    out.writeInt(length)
    work(this)
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    tag match
      case ctag: Choice[a] => work(RawBinaryChoiceWriter(ctag.ordinal(picklee.asInstanceOf[a]), out)) 
      case _ => work(RawBinaryStructureWriter(out))
    this
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    out.writeBoolean(value)
    this
  override def putByte(value: Byte): PickleWriter =
    out.writeInt(value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    out.writeInt(value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    out.writeInt(value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    out.writeInt(value)
    this
  override def putLong(value: Long): PickleWriter =
    out.writeLong(value)
    this
  override def putFloat(value: Float): PickleWriter =
    out.writeFloat(value)
    this
  override def putDouble(value: Double): PickleWriter =
    out.writeDouble(value)
    this
  override def putString(value: String): PickleWriter =
    out.writeString(value)
    this
  override def flush(): Unit = out.flush()

/** 
 * An unknown protocol buffer structure writer.  It simply gives all new fields
 * a new index, starting with 1 and moving up.
 */
class RawBinaryStructureWriter(out: ProtoOutputStream) extends PickleStructureWriter:
  private var currentFieldIndex = 0
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    currentFieldIndex += 1
    pickler(RawBinaryFieldWriter(out, currentFieldIndex))
    this

/** 
 * An unknown protocol buffer structure writer for enums.  It simply looks up the type-level ordinal.
 */
class RawBinaryChoiceWriter(ordinal: Int, out: ProtoOutputStream) extends PickleStructureWriter:
  private var currentFieldIndex = 0
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    pickler(RawBinaryFieldWriter(out, ordinal+1))
    this

class RawBinaryFieldWriter(out: ProtoOutputStream, fieldNum: Int) 
    extends PickleWriter:
  // Writing a collection should simple write a field multiple times.
  // TODO - see if we can determine type and use the alternative encoding.
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work : PickleCollectionWriter => Unit): PickleWriter =
    // Collections are written as the the field number repeated.
    work(RawBinaryCollectionInFieldWriter(out, fieldNum))
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    val sizeEstimate = FieldSizeEstimateWriter(fieldNum, None)
    sizeEstimate.putStructure(picklee, tag)(work)
    out.writeInt(WireFormat.LengthDelimited.makeTag(fieldNum))
    out.writeInt(sizeEstimate.finalSize)
    work(RawBinaryStructureWriter(out))
    this

  override def putUnit(): PickleWriter = 
    // We need to make sure we write a tag/wiretype here
    // TODO - find a less-byte way to do it.
    out.writeInt(fieldNum, 0)
    this
  override def putBoolean(value: Boolean): PickleWriter =
    out.writeBoolean(fieldNum, value)
    this
  override def putByte(value: Byte): PickleWriter =
    out.writeInt(fieldNum, value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    out.writeInt(fieldNum, value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    out.writeInt(fieldNum, value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    out.writeInt(fieldNum, value)
    this
  override def putLong(value: Long): PickleWriter =
    out.writeLong(fieldNum, value)
    this
  override def putFloat(value: Float): PickleWriter =
    out.writeFloat(fieldNum, value)
    this
  override def putDouble(value: Double): PickleWriter =
    out.writeDouble(fieldNum, value)
    this
  override def putString(value: String): PickleWriter =
    out.writeString(fieldNum, value)
    this

  override def flush(): Unit = out.flush()

class RawBinaryCollectionInFieldWriter(out: ProtoOutputStream, fieldNum: Int)
    extends PickleCollectionWriter:
  override def putElement(work: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - we need to know what we're writing here...
    out.writeInt(WireFormat.LengthDelimited.makeTag(fieldNum))
    val sizeEstimate = RawPickleSizeEstimator()
    work(sizeEstimate)
    out.writeInt(sizeEstimate.finalSize)
    work(RawBinaryPickleWriter(out))
    this