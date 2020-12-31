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

  def writeCompressedPrimitives[C: core.CollectionWriter](out: CodedOutputStream, fieldNum: Int, value: C): Unit =
    val writer = summon[core.CollectionWriter[C]]
    out.writeTag(fieldNum, WIRETYPE_LENGTH_DELIMITED)
    // TODO - we want a size estimator for protos w/ descriptors...
    val sizeEstimate = CompressedPrimitiveCollectionSizeEstimator()
    writer.writeCollection(value, sizeEstimate)
    out.writeInt32NoTag(sizeEstimate.finalSize)
    // Write the primitives...
    writer.writeCollection(value, CompressedPrimitiveCollectionWriter(out))

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
  /** Writes raw primitives with no field tags. */
  def writePrimitiveRaw[T](out: CodedOutputStream)(picklee: Any, tag: PrimitiveTag[T]): Unit =
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
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit = summon[core.Writer[T]].write(value, this)
  // TODO - Throw better unsupported operations errors if we don't have the right shape.
  override def writeCollection[T: core.CollectionWriter](value: T): Unit = ???
  override def writeStructure[T: core.StructureWriter ](value: T): Unit = ???
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit = ???
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


/** Calculates the byte length of a a compressed repeated primtiive field. */
class CompressedPrimitiveCollectionSizeEstimator extends PickleCollectionWriter with PickleWriter with SizeEstimator:
  private var size: Int = 0
  def finalSize: Int = size
  override def flush(): Unit = ()
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit = summon[core.Writer[T]].write(value, this)
  // TODO - Throw better unsupported operations errors if we don't have the right shape.
  override def writeCollection[T: core.CollectionWriter](value: T): Unit = ???
  override def writeStructure[T: core.StructureWriter ](value: T): Unit = ???
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit = ???
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