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
    optDescriptor: Option[TypeDescriptorMapping[?]] = None) 
    extends PickleWriter with PickleCollectionWriter
  // Writing a collection should simple write a field multiple times.
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    work(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    optDescriptor match
      case Some(d) =>
        // We need to write a header for this structure proto, which includes its size.
        // For now, we be lazy and write to temporary array, then do it all at once.
        // TODO - figure out if we can precompute and do this faster!
        val tmpByteOut = java.io.ByteArrayOutputStream()
        val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
        work(DescriptorBasedProtoStructureWriter(tmpOut, d))
        tmpOut.flush()
        out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
      // TODO - Better errors.
      case None => throw RuntimeException(s"Cannot find structure definition for: $tag")
    this

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
    pickler(this)
    this

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class DescriptorBasedProtoStructureWriter(
    out: CodedOutputStream,
    mapping: TypeDescriptorMapping[?]) extends PickleStructureWriter
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = mapping.fieldNumber(name)
    pickler(ProtocolBufferFieldWriter(out, idx, mapping.fieldDescriptor(name)))
    this

// TODO - migrate this to be based on TypeDescriptorRepository
class DescriptorBasedProtoWriter(
    out: CodedOutputStream,
    repository: TypeDescriptorRepository
) extends PickleWriter
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    work(DescriptorBasedProtoStructureWriter(out, repository.find(tag)))
    this
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter = ???
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def flush(): Unit = out.flush()
