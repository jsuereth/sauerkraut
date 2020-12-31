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

class ProtocolBufferFieldWriter(
    out: CodedOutputStream, 
    fieldNum: Int,
    // TODO - only allow this for primitives.
    desc: ProtoTypeDescriptor[?]) 
    extends PickleWriter with PickleCollectionWriter:
  // Writing a collection should simple write a field multiple times.
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    val writer = summon[core.CollectionWriter[T]]
    try
      desc.asInstanceOf[CollectionTypeDescriptor[_,_]].element match
        // TODO - if we knew the collection had size == 1 or size == 0 we could optimise how we write it.
        case p: PrimitiveTypeDescriptor[_] => Shared.writeCompressedPrimitives(out, fieldNum, value)
        case elemTag => summon[core.CollectionWriter[T]].writeCollection(value, ProtocolBufferFieldWriter(out, fieldNum, elemTag))
    catch
      case e: ClassCastException =>
        throw new WriteException(s"Could not find collection descriptor, found: $desc", e)
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    try
      // We need to write a header for this structure proto, which includes its size.
      // For now, we be lazy and write to temporary array, then do it all at once.
      // TODO - figure out if we can precompute and do this faster!
      val tmpByteOut = java.io.ByteArrayOutputStream()
      val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
      summon[core.StructureWriter[T]].writeStructure(value, DescriptorBasedProtoStructureWriter(tmpOut, desc.asInstanceOf))
      tmpOut.flush()
      out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
    catch
      case e: ClassCastException =>
        throw WriteException(s"Cannot find structure definition from: $desc", e)
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    val writer = summon[core.ChoiceWriter[T]]
    try
      // We need to write a header for this choice proto which includes its size.
      // For now, we be lazy and write to temporary array, then do it all at once.
      // TODO - figure out if we can precompute and do this faster!
      val tmpByteOut = java.io.ByteArrayOutputStream()
      val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
      writer.writeChoice(value, DescriptorBasedProtoStructureWriter(tmpOut, desc.asInstanceOf))
      tmpOut.flush()
      out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
    catch
      case e: ClassCastException =>
        throw WriteException(s"Cannot find structure definition from: $desc", e)
  override def writeUnit(): Unit = ()
  override def writeBoolean(value: Boolean): Unit = out.writeBool(fieldNum, value)
  override def writeByte(value: Byte): Unit = out.writeInt32(fieldNum, value.toInt)
  override def writeChar(value: Char): Unit = out.writeInt32(fieldNum, value.toInt)
  override def writeShort(value: Short): Unit = out.writeInt32(fieldNum, value.toInt)
  override def writeInt(value: Int): Unit = out.writeInt32(fieldNum, value)
  override def writeLong(value: Long): Unit = out.writeInt64(fieldNum, value)
  override def writeFloat(value: Float): Unit = out.writeFloat(fieldNum, value)
  override def writeDouble(value: Double): Unit = out.writeDouble(fieldNum, value)
  override def writeString(value: String): Unit = out.writeString(fieldNum, value)

  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit =
    summon[core.Writer[T]].write(value, this)

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class DescriptorBasedProtoStructureWriter(
    out: CodedOutputStream,
    mapping: MessageProtoDescriptor[?]) extends PickleStructureWriter with PickleChoiceWriter:
  override def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
    // TODO - uses index from the actual mapping.
    val idx = mapping.fieldNumber(name)
    summon[core.Writer[T]].write(value, ProtocolBufferFieldWriter(out, idx, mapping.fieldDesc(idx)))
  override def writeChoice[T: core.Writer](number: Int, name: String, value: T): Unit =
    // TODO - uses index from the actual mapping.
    val idx = mapping.fieldNumber(name)
    summon[core.Writer[T]].write(value, ProtocolBufferFieldWriter(out, idx, mapping.fieldDesc(idx)))

/** A pickle writer that will only write proto messages using ProtoTypeDescriptors. */
class DescriptorBasedProtoWriter(
    out: CodedOutputStream,
    repository: TypeDescriptorRepository
) extends PickleWriter:
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    val writer = summon[core.StructureWriter[T]]
    try
      writer.writeStructure(value, DescriptorBasedProtoStructureWriter(out, repository.find(writer.tag).asInstanceOf))
    catch
      case e: ClassCastException =>
        throw WriteException(s"Unable to find message descriptor for ${writer.tag}, found ${repository.find(writer.tag)}", e)
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    val writer = summon[core.ChoiceWriter[T]]
    try
      writer.writeChoice(value, DescriptorBasedProtoStructureWriter(out, repository.find(writer.tag).asInstanceOf))
    catch
      case e: ClassCastException =>
        throw WriteException(s"Unable to find message descriptor for ${writer.tag}, found ${repository.find(writer.tag)}", e)
  override def writeCollection[T: core.CollectionWriter](value: T): Unit = ???
  override def writeUnit(): Unit = ???
  override def writeBoolean(value: Boolean): Unit = ???
  override def writeByte(value: Byte): Unit = ???
  override def writeChar(value: Char): Unit = ???
  override def writeShort(value: Short): Unit = ???
  override def writeInt(value: Int): Unit = ???
  override def writeLong(value: Long): Unit = ???
  override def writeFloat(value: Float): Unit = ???
  override def writeDouble(value: Double): Unit = ???
  override def writeString(value: String): Unit = ???
  override def flush(): Unit = out.flush()
