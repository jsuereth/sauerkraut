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

import com.google.protobuf.CodedOutputStream

trait SizeEstimator:
  def finalSize: Int


class RawPickleSizeEstimator extends PickleWriter with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def flush(): Unit = ()
  override def writeUnit(): Unit = ()
  override def writeBoolean(value: Boolean): Unit =
    size += CodedOutputStream.computeBoolSizeNoTag(value)
  override def writeByte(value: Byte): Unit =
    size += CodedOutputStream.computeInt32SizeNoTag(value)
  override def writeChar(value: Char): Unit =
    size += CodedOutputStream.computeInt32SizeNoTag(value)
  override def writeShort(value: Short): Unit =
    size += CodedOutputStream.computeInt32SizeNoTag(value)
  override def writeInt(value: Int): Unit =
    size += CodedOutputStream.computeInt32SizeNoTag(value)
  override def writeLong(value: Long): Unit =
    size += CodedOutputStream.computeInt64SizeNoTag(value)
  override def writeFloat(value: Float): Unit =
    size += CodedOutputStream.computeFloatSizeNoTag(value)
  override def writeDouble(value: Double): Unit =
    size += CodedOutputStream.computeDoubleSizeNoTag(value)
  override def writeString(value: String): Unit =
    size += CodedOutputStream.computeStringSizeNoTag(value)
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    val estimate = RawCollectionSizeEstimateWriter()
    summon[core.CollectionWriter[T]].writeCollection(value, estimate)
    size += estimate.finalSize
  override def writeStructure[T: core.StructureWriter ](value: T): Unit =
    val estimate = SizeEstimateStructureWriter(RawBinaryMessageDescriptor())
    summon[core.StructureWriter[T]].writeStructure(value, estimate)
    size += estimate.finalSize
  override def writeChoice[T: core.ChoiceWriter ](value: T): Unit =
    val estimate = SizeEstimateStructureWriter(RawBinaryMessageDescriptor())
    summon[core.ChoiceWriter[T]].writeChoice(value, estimate)
    size += estimate.finalSize

class RawCollectionSizeEstimateWriter extends PickleCollectionWriter with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def sizeHint(numElements: Int): Unit = 
    size += CodedOutputStream.computeInt32SizeNoTag(numElements)
  override def writeElement[T: core.Writer](value: T): Unit =
    // TODO - implement.  We're getting away with bogus data here...
    val estimate = RawPickleSizeEstimator()
    summon[core.Writer[T]].write(value, estimate)
    size += estimate.finalSize

/** This is a pickle writer that just tries to recursively
 * guess the size of a sub message.
 * 
 * Note: we currently forget subsizes after we've computed
 * them.  That's a huge optimisation win if we can fix it.
 */
class FieldSizeEstimateWriter(fieldNum: Int,
  optDescriptor: Option[ProtoTypeDescriptor[?]])
    extends PickleWriter
    with PickleCollectionWriter
    with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def writeUnit(): Unit =
    size += CodedOutputStream.computeInt32Size(fieldNum, 0)
  override def writeBoolean(value: Boolean): Unit =
    size += CodedOutputStream.computeBoolSize(fieldNum, value)
  override def writeByte(value: Byte): Unit = size += 1
  override def writeChar(value: Char): Unit =
    size += CodedOutputStream.computeInt32Size(fieldNum, value.toInt)
  override def writeShort(value: Short): Unit =
    size += CodedOutputStream.computeInt32Size(fieldNum, value.toInt)
  override def writeInt(value: Int): Unit =
    size += CodedOutputStream.computeInt32Size(fieldNum, value)
  override def writeLong(value: Long): Unit =
    size += CodedOutputStream.computeInt64Size(fieldNum, value)
  override def writeFloat(value: Float): Unit =
    size += CodedOutputStream.computeFloatSize(fieldNum, value)
  override def writeDouble(value: Double): Unit =
    size += CodedOutputStream.computeDoubleSize(fieldNum, value)
  override def writeString(value: String): Unit =
    size += CodedOutputStream.computeStringSize(fieldNum, value)
  // TODO - Primitives behave differently from messages...
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    size += CodedOutputStream.computeTagSize(fieldNum)
    summon[core.CollectionWriter[T]].write(value, this)
  override def sizeHint(numElements: Int): Unit =
    size += CodedOutputStream.computeInt32SizeNoTag(numElements)
  override def writeElement[T: core.Writer](value: T): Unit =
    summon[core.Writer[T]].write(value, this)
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    ???
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    val descriptor = optDescriptor match
      case Some(d: MessageProtoDescriptor[_]) => d
      case _ => RawBinaryMessageDescriptor()
    val subSize =
      val tmp = SizeEstimateStructureWriter(descriptor)
      summon[core.StructureWriter[T]].writeStructure(value, tmp)
      tmp.finalSize
    // Structure are written as follows:
    // [TAG] [SIZE] [RAW BYTES]
    size += CodedOutputStream.computeTagSize(fieldNum)
    size += CodedOutputStream.computeUInt32SizeNoTag(subSize)
    size += subSize
  def flush(): Unit = ()

/** Estimate the size of sub-structure given a TypeDescriptor. */
class SizeEstimateStructureWriter(d: MessageProtoDescriptor[?]) 
    extends PickleStructureWriter
    with PickleChoiceWriter
    with SizeEstimator:
  private var size = 0
  override def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
    val idx = d.fieldNumber(name)
    val fieldPickle = FieldSizeEstimateWriter(idx, Some(d.fieldDesc(idx)))
    summon[core.Writer[T]].write(value, fieldPickle)
    size += fieldPickle.finalSize
  override def writeChoice[T: core.Writer](number: Int, name: String, value: T): Unit =
    val idx = d.fieldNumber(name)
    val fieldPickle = FieldSizeEstimateWriter(idx, Some(d.fieldDesc(idx)))
    summon[core.Writer[T]].write(value, fieldPickle)
    size += fieldPickle.finalSize
  override def finalSize = size



// Used to estimate write-size of protos.  Note: this assumes fields
// are only looked up once, per `putField` call.
class RawBinaryMessageDescriptor[T] extends MessageProtoDescriptor[T]:
  private var currentIdx = 0
  override def fieldDesc[F](num: Int): sauerkraut.format.pb.ProtoTypeDescriptor[F] =
    RawBinaryMessageDescriptor[F]()
  override def fieldNumber(name: String): Int =
    currentIdx += 1
    currentIdx
  // We don't call these methods, normally used in reading.
  override def fieldName(num: Int): String = ???
  override def tag: FastTypeTag[T] = ???