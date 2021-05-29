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

import streams.ProtoOutputStream

class ProtocolBufferFieldWriter(
    out: ProtoOutputStream, 
    fieldNum: Int) 
    extends PickleWriter with PickleCollectionWriter:
  // Writing a collection should simple write a field multiple times.
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    tag.elementTag match
      case p: PrimitiveTag[_] if length > 1 =>
        Shared.writeCompressedPrimitives(out, fieldNum)(work)
      case elemTag => work(ProtocolBufferFieldWriter(out, fieldNum))
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    // We need to write a header for this structure proto, which includes its size.
    // For now, we be lazy and write to temporary array, then do it all at once.
    // TODO - figure out if we can precompute and do this faster!
    val tmpByteOut = java.io.ByteArrayOutputStream()
    val tmpOut = ProtoOutputStream(tmpByteOut)
    work(DescriptorBasedProtoStructureWriter(tmpOut))
    tmpOut.flush()
    out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
    this

  override def putUnit(): PickleWriter = 
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
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - when writing primitive collection, we won't need fieldNum tags.
    pickler(this)
    this

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class DescriptorBasedProtoStructureWriter(out: ProtoOutputStream) extends PickleStructureWriter:
  override def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    pickler(ProtocolBufferFieldWriter(out, number))
    this

/** A pickle writer that will only write proto messages using ProtoTypeDescriptors. */
class DescriptorBasedProtoWriter(
    out: ProtoOutputStream
) extends PickleWriter:
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    work(DescriptorBasedProtoStructureWriter(out))
    this
  override def putUnit(): PickleWriter = ???
  override def putBoolean(value: Boolean): PickleWriter = ???
  override def putByte(value: Byte): PickleWriter = ???
  override def putChar(value: Char): PickleWriter = ???
  override def putShort(value: Short): PickleWriter = ???
  override def putInt(value: Int): PickleWriter = ???
  override def putLong(value: Long): PickleWriter = ???
  override def putFloat(value: Float): PickleWriter = ???
  override def putDouble(value: Double): PickleWriter = ???
  override def putString(value: String): PickleWriter = ???
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def flush(): Unit = out.flush()
