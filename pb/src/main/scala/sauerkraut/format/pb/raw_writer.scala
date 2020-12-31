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
class RawBinaryPickleWriter(out: CodedOutputStream) extends PickleWriter with PickleCollectionWriter:
  override def sizeHint(numElements: Int): Unit = 
    out.writeInt32NoTag(numElements)
  override def writeElement[T: core.Writer](value: T): Unit =
    summon[core.Writer[T]].write(value, this)  
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    summon[core.CollectionWriter[T]].writeCollection(value, this)
  override def writeStructure[T: core.StructureWriter ](value: T): Unit =
    summon[core.StructureWriter[T]].writeStructure(value, RawBinaryStructureWriter(out))
  override def writeChoice[T: core.ChoiceWriter ](value: T): Unit =
    summon[core.ChoiceWriter[T]].writeChoice(value, RawBinaryChoiceWriter(out))
  override def writeUnit(): Unit = ()
  override def writeBoolean(value: Boolean): Unit = out.writeBoolNoTag(value)
  override def writeByte(value: Byte): Unit = out.write(value)
  override def writeChar(value: Char): Unit = out.writeInt32NoTag(value.toInt)
  override def writeShort(value: Short): Unit = out.writeInt32NoTag(value.toInt)
  override def writeInt(value: Int): Unit = out.writeInt32NoTag(value)
  override def writeLong(value: Long): Unit = out.writeInt64NoTag(value)
  override def writeFloat(value: Float): Unit = out.writeFloatNoTag(value)
  override def writeDouble(value: Double): Unit = out.writeDoubleNoTag(value)
  override def writeString(value: String): Unit = out.writeStringNoTag(value)
  override def flush(): Unit = out.flush()

/** 
 * An unknown protocol buffer structure writer.  It simply gives all new fields
 * a new index, starting with 1 and moving up.
 */
class RawBinaryStructureWriter(out: CodedOutputStream) extends PickleStructureWriter:
  override def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
    summon[core.Writer[T]].write(value, RawBinaryFieldWriter(out, number+1))

/** 
 * An unknown protocol buffer structure writer for enums.  It simply looks up the type-level ordinal.
 */
class RawBinaryChoiceWriter(out: CodedOutputStream) extends PickleChoiceWriter:
  override def writeChoice[T: core.Writer](number: Int, name: String, value: T): Unit =
    summon[core.Writer[T]].write(value, RawBinaryFieldWriter(out, number+1))

class RawBinaryFieldWriter(out: CodedOutputStream, fieldNum: Int) 
    extends PickleWriter:
  // Writing a collection should simple write a field multiple times.
  // TODO - see if we can determine type and use the alternative encoding.
  override def writeStructure[T: core.StructureWriter ](value: T): Unit =
    val structureWriter = summon[core.StructureWriter[T]]
    val sizeEstimate = FieldSizeEstimateWriter(fieldNum, None)
    sizeEstimate.writeStructure(value)
    out.writeTag(fieldNum, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    structureWriter.writeStructure(value, RawBinaryStructureWriter(out))
  override def writeChoice[T: core.ChoiceWriter ](value: T): Unit =
    val choiceWriter = summon[core.ChoiceWriter[T]]
    val sizeEstimate = FieldSizeEstimateWriter(fieldNum, None)
    sizeEstimate.writeChoice(value)
    out.writeTag(fieldNum, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    choiceWriter.writeChoice(value, RawBinaryChoiceWriter(out))
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    summon[core.CollectionWriter[T]].writeCollection(value, RawBinaryCollectionInFieldWriter(out, fieldNum))
  override def writeUnit(): Unit =
    // We need to make sure we write a tag/wiretype here
    // TODO - find a less-byte way to do it.
    out.writeInt32(fieldNum, 0)
  override def writeBoolean(value: Boolean): Unit = out.writeBool(fieldNum, value)
  override def writeByte(value: Byte): Unit = out.writeInt32(fieldNum, value)
  override def writeChar(value: Char): Unit = out.writeInt32(fieldNum, value)
  override def writeShort(value: Short): Unit = out.writeInt32(fieldNum, value)
  override def writeInt(value: Int): Unit = out.writeInt32(fieldNum, value)
  override def writeLong(value: Long): Unit = out.writeInt64(fieldNum, value)
  override def writeFloat(value: Float): Unit = out.writeFloat(fieldNum, value)
  override def writeDouble(value: Double): Unit = out.writeDouble(fieldNum, value)
  override def writeString(value: String): Unit = out.writeString(fieldNum, value)
  override def flush(): Unit = out.flush()

class RawBinaryCollectionInFieldWriter(out: CodedOutputStream, fieldNum: Int)
    extends PickleCollectionWriter:
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit =
    val writer = summon[core.Writer[T]]
    out.writeTag(fieldNum, WireFormat.WIRETYPE_LENGTH_DELIMITED)
    val sizeEstimate = RawPickleSizeEstimator()
    writer.write(value, sizeEstimate)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    writer.write(value, RawBinaryPickleWriter(out))
