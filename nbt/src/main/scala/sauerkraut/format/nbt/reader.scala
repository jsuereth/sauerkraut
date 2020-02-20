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

import internal.TagInputStream
import internal.NbtTag

class NbtPickleReader(in: TagInputStream)
    extends PickleReader
  override def push[T](b: core.Builder[T]): core.Builder[T] =
    // We ignore tags and rely on types to figure stuff out.
    b match
      // Unit has no tag or value at runtime.
      case p: core.PrimitiveBuilder[?] if p.tag == PrimitiveTag.UnitTag => ()
      case _ => in.readTag()
    // TODO - Assert tag is expected for this type.
    try readPayloadFor(b)
    catch
      case e: java.io.EOFException =>
        Console.err.println(s"Caught EOF while building: $b: ${b.result}")
        throw e

  private def readPayloadFor[T](b: core.Builder[T]): core.Builder[T] =
    b match
      case s: core.StructureBuilder[T] => readStructure(s)
      case c: core.CollectionBuilder[_, T] => readCollection(c)
      case p: core.PrimitiveBuilder[T] => readPrimitive(p)
    b
  private def readPrimitive[T](b: core.PrimitiveBuilder[T]): Unit =
     if (b.tag != PrimitiveTag.UnitTag)
       b.putPrimitive(in.readPayload(b.tag))
  private def readCollection[El, To](b: core.CollectionBuilder[El, To]): Unit =
    // Read type of elements in the collection.
    in.readTag() // TODO - assert
    var length = in.readLength()
    while (length > 0)
      readPayloadFor(b.putElement())
      length -= 1
  private def readStructure[T](p: core.StructureBuilder[T]): Unit =
    // Read the tag of the first named value.
    var currentTag = in.readTag()
    while (currentTag != NbtTag.TagEnd)
      // Read the name component
      val name = in.readName()
      // TODO - figure out how to supress reading more tags.
      readPayloadFor(p.putField(name))
      currentTag = in.readTag()
