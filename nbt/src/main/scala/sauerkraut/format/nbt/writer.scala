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
  private def optWriteName(): Unit =
    optName match
      case Some(name) => out.writeStringPayload(name)
      case None => ()
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    import PrimitiveTag._
    tag match
      case UnitTag => ()
      case BooleanTag => 
        out.writeRawTag(NbtTag.TagByte)
        optWriteName()
        out.writeBytePayload(
            if picklee.asInstanceOf[Boolean] 
            then 1.toByte 
            else 0.toByte)
      case ByteTag =>
        out.writeRawTag(NbtTag.TagByte)
        optWriteName()
        out.writeBytePayload(picklee.asInstanceOf[Byte])
      case CharTag =>
        out.writeRawTag(NbtTag.TagShort)
        optWriteName()
        out.writeShortPayload(picklee.asInstanceOf[Char].toShort)
      case ShortTag =>
        out.writeRawTag(NbtTag.TagShort)
        optWriteName()
        out.writeShortPayload(picklee.asInstanceOf[Short])
      case IntTag =>
        out.writeRawTag(NbtTag.TagInt)
        optWriteName()
        out.writeIntPayload(picklee.asInstanceOf[Int])
      case LongTag =>
        out.writeRawTag(NbtTag.TagLong)
        optWriteName()
        out.writeLongPayload(picklee.asInstanceOf[Long])
      case FloatTag =>
        out.writeRawTag(NbtTag.TagFloat)
        optWriteName()
        out.writeFloatPayload(picklee.asInstanceOf[Float])
      case DoubleTag =>
        out.writeRawTag(NbtTag.TagDouble)
        optWriteName()
        out.writeDoublePayload(picklee.asInstanceOf[Double])
      case StringTag =>
        out.writeRawTag(NbtTag.TagString)
        optWriteName()
        out.writeStringPayload(picklee.asInstanceOf[String])
    this
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    ???
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