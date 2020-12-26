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
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    try
      desc.asInstanceOf[CollectionTypeDescriptor[_,_]].element match
        case p: PrimitiveTypeDescriptor[_] if length > 1 => Shared.writeCompressedPrimitives(out, fieldNum)(work)
        case elemTag => work(ProtocolBufferFieldWriter(out, fieldNum, elemTag))
      this
    catch
      case e: ClassCastException =>
        throw new WriteException(s"Could not find collection descriptor, found: $desc", e)
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    try
      // We need to write a header for this structure proto, which includes its size.
      // For now, we be lazy and write to temporary array, then do it all at once.
      // TODO - figure out if we can precompute and do this faster!
      val tmpByteOut = java.io.ByteArrayOutputStream()
      val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
      work(DescriptorBasedProtoStructureWriter(tmpOut, desc.asInstanceOf))
      tmpOut.flush()
      out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
      this
    catch
      case e: ClassCastException =>
        throw WriteException(s"Cannot find structure definition from: $desc", e)

  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => out.writeBool(fieldNum, picklee.asInstanceOf[Boolean])
      case PrimitiveTag.ByteTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Byte].toInt)
      case PrimitiveTag.CharTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Char].toInt)
      case PrimitiveTag.ShortTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Short].toInt)
      case PrimitiveTag.IntTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Int])
      case PrimitiveTag.LongTag => out.writeInt64(fieldNum, picklee.asInstanceOf[Long])
      case PrimitiveTag.FloatTag => out.writeFloat(fieldNum, picklee.asInstanceOf[Float])
      case PrimitiveTag.DoubleTag => out.writeDouble(fieldNum, picklee.asInstanceOf[Double])
      case PrimitiveTag.StringTag => out.writeString(fieldNum, picklee.asInstanceOf[String])
    this

  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - when writing primitive collection, we won't need fieldNum tags.
    pickler(this)
    this

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class DescriptorBasedProtoStructureWriter(
    out: CodedOutputStream,
    mapping: MessageProtoDescriptor[?]) extends PickleStructureWriter:
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = mapping.fieldNumber(name)
    pickler(ProtocolBufferFieldWriter(out, idx, mapping.fieldDesc(idx)))
    this

/** A pickle writer that will only write proto messages using ProtoTypeDescriptors. */
class DescriptorBasedProtoWriter(
    out: CodedOutputStream,
    repository: TypeDescriptorRepository
) extends PickleWriter:
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    try
      work(DescriptorBasedProtoStructureWriter(out, repository.find(tag).asInstanceOf))
      this
    catch
      case e: ClassCastException =>
        throw WriteException(s"Unable to find message descriptor for $tag, found ${repository.find(tag)}", e)
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter = ???
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def flush(): Unit = out.flush()
