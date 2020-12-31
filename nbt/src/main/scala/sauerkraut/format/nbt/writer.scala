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
    with PickleStructureWriter
    with PickleChoiceWriter:
  private def optWriteName(): Unit =
    optName match
      case Some(name) => out.writeStringPayload(name)
      case None => ()
  override def writeUnit(): Unit = ()
  override def writeBoolean(value: Boolean): Unit =
    out.writeRawTag(NbtTag.TagByte)
    optWriteName()
    out.writeBytePayload(if value then 1 else 0)
  override def writeByte(value: Byte): Unit =
    out.writeRawTag(NbtTag.TagByte)
    optWriteName()
    out.writeBytePayload(value)
  override def writeChar(value: Char): Unit =
    out.writeRawTag(NbtTag.TagShort)
    optWriteName()
    out.writeShortPayload(value.toShort)
  override def writeShort(value: Short): Unit =
    out.writeRawTag(NbtTag.TagShort)
    optWriteName()
    out.writeShortPayload(value)
  override def writeInt(value: Int): Unit =
    out.writeRawTag(NbtTag.TagInt)
    optWriteName()
    out.writeIntPayload(value)
  override def writeLong(value: Long): Unit =
    out.writeRawTag(NbtTag.TagLong)
    optWriteName()
    out.writeLongPayload(value)
  override def writeFloat(value: Float): Unit =
    out.writeRawTag(NbtTag.TagFloat)
    optWriteName()
    out.writeFloatPayload(value)
  override def writeDouble(value: Double): Unit =
    out.writeRawTag(NbtTag.TagDouble)
    optWriteName()
    out.writeDoublePayload(value)
  override def writeString(value: String): Unit =
    out.writeRawTag(NbtTag.TagString)
    optWriteName()
    out.writeStringPayload(value)
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    // We defer writing a tag until we know the collection type.    
    out.writeRawTag(NbtTag.TagList)
    optWriteName()
    summon[core.CollectionWriter[T]].writeCollection(value, NbtCollectionWriter(out))
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    out.writeRawTag(NbtTag.TagCompound)
    optWriteName()
    summon[core.StructureWriter[T]].writeStructure(value, this)
    out.writeRawTag(NbtTag.TagEnd)
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    // Names get embedded after type tag, so we defer writing it.
    out.writeRawTag(NbtTag.TagCompound)
    optWriteName()
    summon[core.ChoiceWriter[T]].writeChoice(value, this)
    out.writeRawTag(NbtTag.TagEnd)
  override def writeField[T: core.Writer](num: Int, name: String, value: T): Unit =
    summon[core.Writer[T]].write(value, NbtPickleWriter(out, Some(name)))
  override def writeChoice[T: core.Writer](num: Int, name: String, value: T): Unit =
    summon[core.Writer[T]].write(value, NbtPickleWriter(out, Some(name)))
    // Names get embedded after type tag, so we defer writing it.
  override def flush(): Unit = out.flush()

class NbtCollectionWriter(out: TagOutputStream)
  extends PickleCollectionWriter
  with PickleWriter:
  private var hasHeader: Boolean = false
  private var length: Option[Int] = None
  private inline def optHeader(writeHeader: => Unit): Unit =
     if (!hasHeader)
       writeHeader
       out.writeIntPayload(length.getOrElse(throw WriteException("NBT format requires sizeHint from collections", null)))
       hasHeader = true
  override def sizeHint(numElements: Int): Unit =
    length = Some(numElements)
  override def writeElement[T: core.Writer](value: T): Unit =
    summon[core.Writer[T]].write(value, this)
  override def writeUnit(): Unit = throw WriteException("Writing type Unit `()` is not allowed in nbt format collections", null)
  override def writeBoolean(value: Boolean): Unit =
    optHeader(out.writeRawTag(NbtTag.TagByte))
    out.writeBytePayload(if value then 1 else 0)
  override def writeByte(value: Byte): Unit =
    optHeader(out.writeRawTag(NbtTag.TagByte))
    out.writeBytePayload(value)
  override def writeChar(value: Char): Unit =
    optHeader(out.writeRawTag(NbtTag.TagShort))
    out.writeShortPayload(value.toShort)
  override def writeShort(value: Short): Unit =
    optHeader(out.writeRawTag(NbtTag.TagShort))
    out.writeShortPayload(value)
  override def writeInt(value: Int): Unit =
    optHeader(out.writeRawTag(NbtTag.TagInt))
    out.writeIntPayload(value)
  override def writeLong(value: Long): Unit =
    optHeader(out.writeRawTag(NbtTag.TagLong))
    out.writeLongPayload(value)
  override def writeFloat(value: Float): Unit =
    optHeader(out.writeRawTag(NbtTag.TagFloat))
    out.writeFloatPayload(value)
  override def writeDouble(value: Double): Unit =
    optHeader(out.writeRawTag(NbtTag.TagDouble))
    out.writeDoublePayload(value)
  override def writeString(value: String): Unit =
    optHeader(out.writeRawTag(NbtTag.TagString))
    out.writeStringPayload(value)
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    // We defer writing a tag until we know the collection type.
    optHeader(out.writeRawTag(NbtTag.TagList))
    summon[core.CollectionWriter[T]].writeCollection(value, NbtCollectionWriter(out))
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    optHeader(out.writeRawTag(NbtTag.TagCompound))
    summon[core.StructureWriter[T]].writeStructure(value, NbtPickleWriter(out))
    out.writeRawTag(NbtTag.TagEnd)
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    optHeader(out.writeRawTag(NbtTag.TagCompound))
    summon[core.ChoiceWriter[T]].writeChoice(value, NbtPickleWriter(out))
    out.writeRawTag(NbtTag.TagEnd)
  override def flush(): Unit = out.flush()
  