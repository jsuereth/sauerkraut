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

/**
 * A PickleWriter that writes protocol-buffer-like pickles.   This will NOT
 * lookup appropriate field numbers per type, but instead number fields in order
 * it sees them as 1->N. This is ok for ephemeral serialization where there is no
 * class/definition skew, but not ok in most serialization applications.
 */
class RawBinaryPickleWriter(out: CodedOutputStream) extends PickleWriter with PickleCollectionWriter
  def beginCollection(length: Int): PickleCollectionWriter =
    // When writing 'raw' collections, we just write a length, then each element.
    out.writeInt32NoTag(length)
    this
  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  def endCollection(): Unit = ()

  // TODO - lookup known structure before using this.
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    RawBinaryStructureWriter(out)
  def putPrimitive(picklee: Any, tag: FastTypeTag[?]): Unit =
    tag match
      case FastTypeTag.UnitTag => ()
      case FastTypeTag.BooleanTag => out.writeBoolNoTag(picklee.asInstanceOf[Boolean])
      case FastTypeTag.CharTag => out.writeInt32NoTag(picklee.asInstanceOf[Char].toInt)
      case FastTypeTag.ShortTag => out.writeInt32NoTag(picklee.asInstanceOf[Short].toInt)
      case FastTypeTag.IntTag => out.writeInt32NoTag(picklee.asInstanceOf[Int])
      case FastTypeTag.LongTag => out.writeInt64NoTag(picklee.asInstanceOf[Long])
      case FastTypeTag.FloatTag => out.writeFloatNoTag(picklee.asInstanceOf[Float])
      case FastTypeTag.DoubleTag => out.writeDoubleNoTag(picklee.asInstanceOf[Double])
      case FastTypeTag.StringTag => out.writeStringNoTag(picklee.asInstanceOf[String])
      case FastTypeTag.Named(name) => ???
  override def flush(): Unit = out.flush()

/** 
 * An unknown protocol buffer structure writer.  It simply gives all new fields
 * a new index, starting with 1 and moving up.
 */
class RawBinaryStructureWriter(out: CodedOutputStream) extends PickleStructureWriter
  private var currentFieldIndex = 0
  def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    currentFieldIndex += 1
    pickler(RawBinaryFieldWriter(out, currentFieldIndex))
    this
  def endStructure(): Unit = ()


class RawBinaryFieldWriter(out: CodedOutputStream, fieldNum: Int) 
    extends PickleWriter with PickleCollectionWriter
  // Writing a collection should simple write a field multiple times.
  def beginCollection(length: Int): PickleCollectionWriter =
    this
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    RawBinaryStructureWriter(out)

  def putPrimitive(picklee: Any, tag: FastTypeTag[?]): Unit =
    tag match
      case FastTypeTag.UnitTag => ()
      case FastTypeTag.BooleanTag => out.writeBool(fieldNum, picklee.asInstanceOf[Boolean])
      case FastTypeTag.CharTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Char].toInt)
      case FastTypeTag.ShortTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Short].toInt)
      case FastTypeTag.IntTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Int])
      case FastTypeTag.LongTag => out.writeInt64(fieldNum, picklee.asInstanceOf[Long])
      case FastTypeTag.FloatTag => out.writeFloat(fieldNum, picklee.asInstanceOf[Float])
      case FastTypeTag.DoubleTag => out.writeDouble(fieldNum, picklee.asInstanceOf[Double])
      case FastTypeTag.StringTag => out.writeString(fieldNum, picklee.asInstanceOf[String])
      case FastTypeTag.Named(name) => ???

  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this

  def endCollection(): Unit = ()

  override def flush(): Unit = out.flush()
