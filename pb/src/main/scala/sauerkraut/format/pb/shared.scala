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

package sauerkraut
package format
package pb

import com.google.protobuf.{
  CodedInputStream,
  CodedOutputStream,
  WireFormat
}
import WireFormat.{
  WIRETYPE_LENGTH_DELIMITED
}

/** Helper methods for implementing pb + raw protocols. */
object Shared:
  /** Reads a compressed repeated primitive field. */
  def readCompressedPrimitive[E, To](in: CodedInputStream)(b: core.CollectionBuilder[E, To], elementTag: PrimitiveTag[E]): Unit =
    limitByWireType(in)(WIRETYPE_LENGTH_DELIMITED) {
      while (!in.isAtEnd()) {
        readPrimitive(in)(b.putElement().asInstanceOf)
      }
    }

  def writeCompressedPrimitives[E, To](out: CodedOutputStream, fieldNum: Int)(
    work: PickleCollectionWriter => Unit): Unit =
    out.writeTag(fieldNum, WIRETYPE_LENGTH_DELIMITED)
    // TODO - we want a size estimator for protos w/ descriptors...
    val sizeEstimate = CompressedPrimitiveCollectionSizeEstimator()
    work(sizeEstimate)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    // Write the primitives...
    work(CompressedPrimitiveCollectionWriter(out))

  /** Reads a primitive by using the Builder's tag to determine how to interpret the data. */
  def readPrimitive[T](in: CodedInputStream)(b: core.PrimitiveBuilder[T]): Unit =
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
  
  inline def limitByWireType[A](in: CodedInputStream)(wireType: Int)(f: => A): Unit =
    // TODO - if field is a STRING we do not limit by length.
    if wireType == WIRETYPE_LENGTH_DELIMITED
    then
      var length = in.readRawVarint32()
      val limit = in.pushLimit(length)
      f
      in.popLimit(limit)
    else f

/** Pickle writer that can only write compressed repeated primitive fields. */
class CompressedPrimitiveCollectionWriter(out: CodedOutputStream) extends PickleCollectionWriter with PickleWriter:
  override def flush(): Unit = out.flush()
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  // TODO - Throw better unsupported operations errors if we don't have the right shape.
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(pickler: PickleStructureWriter => Unit): PickleWriter = ???
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    out.writeBoolNoTag(value)
    this
  override def putByte(value: Byte): PickleWriter =
    out.writeInt32NoTag(value.toInt)
    this
  override def putChar(value: Char): PickleWriter = 
    out.writeInt32NoTag(value.toInt)
    this
  override def putShort(value: Short): PickleWriter =
    out.writeInt32NoTag(value.toInt)
    this
  override def putInt(value: Int): PickleWriter = 
    out.writeInt32NoTag(value)
    this
  override def putLong(value: Long): PickleWriter =
    out.writeInt64NoTag(value)
    this
  override def putFloat(value: Float): PickleWriter =
    out.writeFloatNoTag(value)
    this
  override def putDouble(value: Double): PickleWriter =
    out.writeDoubleNoTag(value)
    this
  override def putString(value: String): PickleWriter =
    out.writeStringNoTag(value)
    this


/** Calculates the byte length of a a compressed repeated primtiive field. */
class CompressedPrimitiveCollectionSizeEstimator extends PickleCollectionWriter with PickleWriter with SizeEstimator:
  private var size: Int = 0
  def finalSize: Int = size
  override def flush(): Unit = ()

  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  // TODO - Throw better unsupported operations errors if we don't have the right shape.
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(pickler: PickleStructureWriter => Unit): PickleWriter = ???
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
  