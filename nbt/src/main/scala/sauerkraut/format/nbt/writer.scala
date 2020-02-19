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
    out.writeTag(tag)
    optWriteName()
    out.writePayload[Any](picklee, tag.asInstanceOf)
    this
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    // We defer writing a tag until we know the collection type.
    out.writeRawTag(NbtTag.TagList)
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
  with PickleWriter
  private var hasHeader: Boolean = false
  private def optHeader(writeHeader: => Unit): Unit =
     if (!hasHeader)
       writeHeader
       out.writeIntPayload(length)
       hasHeader = true
  override def putElement(work: PickleWriter => Unit): PickleCollectionWriter =
    work(this)
    this
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    optHeader(out.writeTag(tag))
    out.writePayload[Any](picklee, tag.asInstanceOf)
    this
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
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
  