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

import streams.{
  ProtoWireSize,
  WireFormat
}

trait SizeEstimator:
  def finalSize: Int


class RawPickleSizeEstimator extends PickleWriter with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def flush(): Unit = ()
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    size += ProtoWireSize.sizeOf(length)
    val estimate = RawCollectionSizeEstimateWriter()
    work(estimate)
    size += estimate.finalSize
    this
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    size += ProtoWireSize.sizeOf(value)
    this
  override def putByte(value: Byte): PickleWriter =
    size += ProtoWireSize.sizeOf(value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    size += ProtoWireSize.sizeOf(value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    size += ProtoWireSize.sizeOf(value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    size += ProtoWireSize.sizeOf(value)
    this
  override def putLong(value: Long): PickleWriter =
    size += ProtoWireSize.sizeOf(value)
    this
  override def putFloat(value: Float): PickleWriter =
    size += ProtoWireSize.sizeOf(value)
    this
  override def putDouble(value: Double): PickleWriter =
    size += ProtoWireSize.sizeOf(value)
    this
  override def putString(value: String): PickleWriter =
    size += ProtoWireSize.sizeOf(value)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(pickler: PickleStructureWriter => Unit): PickleWriter =
    val estimate = SizeEstimateStructureWriter()
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
class FieldSizeEstimateWriter(fieldNum: Int)
    extends PickleWriter
    with PickleCollectionWriter
    with SizeEstimator:
  private var size: Int = 0
  override def finalSize: Int = size
  override def putUnit(): PickleWriter = 
    size += ProtoWireSize.sizeOf(fieldNum, 0)
    this
  override def putBoolean(value: Boolean): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  override def putByte(value: Byte): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    size += ProtoWireSize.sizeOf(fieldNum, value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  override def putLong(value: Long): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  override def putFloat(value: Float): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  override def putDouble(value: Double): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  override def putString(value: String): PickleWriter =
    size += ProtoWireSize.sizeOf(fieldNum, value)
    this
  // TODO - Primitives behave differently from messages...
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter = 
    size += ProtoWireSize.sizeOfTag(WireFormat.LengthDelimited, fieldNum)
    size += ProtoWireSize.sizeOf(length)
    work(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    val subSize =
      val tmp = SizeEstimateStructureWriter()
      work(tmp)
      tmp.finalSize
    // Structure are written as follows:
    // [TAG] [SIZE] [RAW BYTES]
    size += ProtoWireSize.sizeOfTag(WireFormat.LengthDelimited, fieldNum)
    size += ProtoWireSize.sizeOf(subSize)
    size += subSize
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  def flush(): Unit = ()

/** Estimate the size of sub-structure given a TypeDescriptor. */
class SizeEstimateStructureWriter() 
    extends PickleStructureWriter
    with SizeEstimator:
  private var size = 0
  override def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val fieldPickle = FieldSizeEstimateWriter(number)
    pickler(fieldPickle)
    size += fieldPickle.finalSize
    this
  override def finalSize = size
