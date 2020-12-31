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
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    size += CodedOutputStream.computeInt32SizeNoTag(length)
    val estimate = RawCollectionSizeEstimateWriter()
    work(estimate)
    size += estimate.finalSize
    this
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    size += CodedOutputStream.computeBoolSizeNoTag(value)
    this
  override def putByte(value: Byte): PickleWriter =
    size += CodedOutputStream.computeInt32SizeNoTag(value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    size += CodedOutputStream.computeInt32SizeNoTag(value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    size += CodedOutputStream.computeInt32SizeNoTag(value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    size += CodedOutputStream.computeInt32SizeNoTag(value)
    this
  override def putLong(value: Long): PickleWriter =
    size += CodedOutputStream.computeInt64SizeNoTag(value)
    this
  override def putFloat(value: Float): PickleWriter =
    size += CodedOutputStream.computeFloatSizeNoTag(value)
    this
  override def putDouble(value: Double): PickleWriter =
    size += CodedOutputStream.computeDoubleSizeNoTag(value)
    this
  override def putString(value: String): PickleWriter =
    size += CodedOutputStream.computeStringSizeNoTag(value)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(pickler: PickleStructureWriter => Unit): PickleWriter =
    val estimate = SizeEstimateStructureWriter(RawBinaryMessageDescriptor())
    pickler(estimate)
    size += estimate.finalSize
    this

class RawCollectionSizeEstimateWriter extends PickleCollectionWriter with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - implement.  We're getting away with bogus data here...
    val estimate = RawPickleSizeEstimator()
    pickler(estimate)
    size += estimate.finalSize
    this

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
  override def putUnit(): PickleWriter = 
    size += CodedOutputStream.computeInt32Size(fieldNum, 0)
    this
  override def putBoolean(value: Boolean): PickleWriter =
    size += CodedOutputStream.computeBoolSize(fieldNum, value)
    this
  override def putByte(value: Byte): PickleWriter =
    size += CodedOutputStream.computeInt32Size(fieldNum, value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    size += CodedOutputStream.computeInt32Size(fieldNum, value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    size += CodedOutputStream.computeInt32Size(fieldNum, value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    size += CodedOutputStream.computeInt32Size(fieldNum, value)
    this
  override def putLong(value: Long): PickleWriter =
    size += CodedOutputStream.computeInt64Size(fieldNum, value)
    this
  override def putFloat(value: Float): PickleWriter =
    size += CodedOutputStream.computeFloatSize(fieldNum, value)
    this
  override def putDouble(value: Double): PickleWriter =
    size += CodedOutputStream.computeDoubleSize(fieldNum, value)
    this
  override def putString(value: String): PickleWriter =
    size += CodedOutputStream.computeStringSize(fieldNum, value)
    this
  // TODO - Primitives behave differently from messages...
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter = 
    size += CodedOutputStream.computeTagSize(fieldNum)
    size += CodedOutputStream.computeInt32SizeNoTag(length)
    work(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter = 
    val descriptor = optDescriptor match
      case Some(d: MessageProtoDescriptor[_]) => d
      case _ => RawBinaryMessageDescriptor()
    val subSize =
      val tmp = SizeEstimateStructureWriter(descriptor)
      work(tmp)
      tmp.finalSize
    // Structure are written as follows:
    // [TAG] [SIZE] [RAW BYTES]
    size += CodedOutputStream.computeTagSize(fieldNum)
    size += CodedOutputStream.computeUInt32SizeNoTag(subSize)
    size += subSize
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  def flush(): Unit = ()

/** Estimate the size of sub-structure given a TypeDescriptor. */
class SizeEstimateStructureWriter(d: MessageProtoDescriptor[?]) 
    extends PickleStructureWriter
    with SizeEstimator:
  private var size = 0
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = d.fieldNumber(name)
    val fieldPickle = FieldSizeEstimateWriter(idx, Some(d.fieldDesc(idx)))
    pickler(fieldPickle)
    size += fieldPickle.finalSize
    this
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