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
package nbt

import internal.{
    NbtTag,
    TagOutputStream
}
 

class NbtPickleWriter(out: TagOutputStream, optName: Option[String] = None)
    extends PickleWriter
    with PickleStructureWriter:
  private inline def optWriteName(): Unit =
    optName match
      case Some(name) => out.writeStringPayload(name)
      case None => ()
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    out.writeRawTag(NbtTag.TagByte)
    optWriteName()
    out.writeBytePayload(if value then 1 else 0)
    this
  override def putByte(value: Byte): PickleWriter =
    out.writeRawTag(NbtTag.TagByte)
    optWriteName()
    out.writeBytePayload(value)
    this
  override def putChar(value: Char): PickleWriter = 
    out.writeRawTag(NbtTag.TagShort)
    optWriteName()
    out.writeShortPayload(value.toShort)
    this
  override def putShort(value: Short): PickleWriter =
    out.writeRawTag(NbtTag.TagShort)
    optWriteName()
    out.writeShortPayload(value)
    this
  override def putInt(value: Int): PickleWriter = 
    out.writeRawTag(NbtTag.TagInt)
    optWriteName()
    out.writeIntPayload(value)
    this
  override def putLong(value: Long): PickleWriter =
    out.writeRawTag(NbtTag.TagLong)
    optWriteName()
    out.writeLongPayload(value)
    this
  override def putFloat(value: Float): PickleWriter =
    out.writeRawTag(NbtTag.TagFloat)
    optWriteName()
    out.writeFloatPayload(value)
    this
  override def putDouble(value: Double): PickleWriter =
    out.writeRawTag(NbtTag.TagDouble)
    optWriteName()
    out.writeDoublePayload(value)
    this
  override def putString(value: String): PickleWriter =
    out.writeRawTag(NbtTag.TagString)
    optWriteName()
    out.writeStringPayload(value)
    this
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    // We defer writing a tag until we know the collection type.
    out.writeRawTag(NbtTag.TagList)
    optWriteName()
    work(NbtCollectionWriter(out, length))
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    out.writeRawTag(NbtTag.TagCompound)
    optWriteName()
    work(this)
    out.writeRawTag(NbtTag.TagEnd)
    this
  override def flush(): Unit = out.flush()

  // Structure writing can go here.
  override def putField(name: String, fieldWriter: PickleWriter => Unit): PickleStructureWriter =
    fieldWriter(NbtPickleWriter(out, Some(name)))
    this

class NbtCollectionWriter(
    out: TagOutputStream,
    length: Int)
  extends PickleCollectionWriter
  with PickleWriter:
  private var hasHeader: Boolean = false
  private inline def optHeader(writeHeader: => Unit): Unit =
     if (!hasHeader)
       writeHeader
       out.writeIntPayload(length)
       hasHeader = true
  override def putElement(work: PickleWriter => Unit): PickleCollectionWriter =
    work(this)
    this 
  // TODO - maybe don't rely on toString on primitives...
  override def putUnit(): PickleWriter = 
    this
  override def putBoolean(value: Boolean): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagByte))
    out.writeBytePayload(if value then 1 else 0)
    this
  override def putByte(value: Byte): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagByte))
    out.writeBytePayload(value)
    this
  override def putChar(value: Char): PickleWriter = 
    optHeader(out.writeRawTag(NbtTag.TagShort))
    out.writeShortPayload(value.toShort)
    this
  override def putShort(value: Short): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagShort))
    out.writeShortPayload(value)
    this
  override def putInt(value: Int): PickleWriter = 
    optHeader(out.writeRawTag(NbtTag.TagInt))
    out.writeIntPayload(value)
    this
  override def putLong(value: Long): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagLong))
    out.writeLongPayload(value)
    this
  override def putFloat(value: Float): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagFloat))
    out.writeFloatPayload(value)
    this
  override def putDouble(value: Double): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagDouble))
    out.writeDoublePayload(value)
    this
  override def putString(value: String): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagString))
    out.writeStringPayload(value)
    this

  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    // We defer writing a tag until we know the collection type.
    optHeader(out.writeRawTag(NbtTag.TagList))
    work(NbtCollectionWriter(out, length))
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    optHeader(out.writeRawTag(NbtTag.TagCompound))
    work(NbtPickleWriter(out))
    out.writeRawTag(NbtTag.TagEnd)
    this
  override def flush(): Unit = out.flush()
  