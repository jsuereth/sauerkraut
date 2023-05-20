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

import streams.{ProtoOutputStream, WireFormat}
import sauerkraut.format.PickleCollectionWriter
import sauerkraut.format.PickleStructureWriter
import sauerkraut.format.pretty.Pretty
import sauerkraut.utils.InlineWriter

trait ProtoSerializationCache:
  def getCachedSize(picklee: Any): Option[Int]
  def cacheSize(picklee: Any, size: Int): Unit
  def cachedUtf8(value: String): Array[Byte]

  inline def cachedSize(picklee: Any, inline size: => Int): Int =
    getCachedSize(picklee) match
      case Some(value) => value
      case None =>
        val result = size
        cacheSize(picklee, result)
        result

object NoProtoSerializationCache extends ProtoSerializationCache:
  override def getCachedSize(picklee: Any): Option[Int] = None
  override def cacheSize(picklee: Any, size: Int): Unit = ()
  override def cachedUtf8(value: String): Array[Byte] = value.getBytes(InlineWriter.Utf8)

class SimpleProtoSerializationCache extends ProtoSerializationCache:
  private val sizeCache: collection.mutable.Map[Any, Int] = collection.mutable.HashMap()
  private val stringCache: collection.mutable.Map[String, Array[Byte]] = collection.mutable.HashMap()
  override def getCachedSize(picklee: Any): Option[Int] =
    sizeCache get picklee
  override def cacheSize(picklee: Any, size: Int): Unit =
    sizeCache.put(picklee, size)
  override def cachedUtf8(value: String): Array[Byte] =
    stringCache.getOrElseUpdate(value, value.getBytes(InlineWriter.Utf8))

/** 
 * A writer where the pickle is included as a field in an outer structure. 
 * 
 * @param out The output stream
 * @param fieldNum the number of the field being written.
 */
class ProtoFieldWriter(
    out: ProtoOutputStream, 
    fieldNum: Int,
    cache: ProtoSerializationCache = SimpleProtoSerializationCache())
    extends PickleWriter with PickleCollectionWriter:
  // Writing a collection should simple write a field multiple times.
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    tag.elementTag match
      case p: PrimitiveTag[_] if length > 1 =>
        Shared.writeCompressedPrimitives(out, fieldNum)(work)
      case elemTag => work(this)
    this
  override def putStructure(picklee: Any, tag: Struct[?])(work: PickleStructureWriter => Unit): PickleWriter =
    // We need to write a header for this structure proto, which includes its size.
    // For now, we be lazy and write to temporary array, then do it all at once.
    // TODO - figure out if we can precompute and do this faster!
    val size = cache.cachedSize(picklee, streams.sizeOfStruct(cache, work))
    out.writeInt(WireFormat.LengthDelimited.makeTag(fieldNum))
    out.writeInt(size)
    work(ProtoStructureWriter(out, cache))
//    val tmpByteOut = java.io.ByteArrayOutputStream()
//    val tmpOut = ProtoOutputStream(tmpByteOut)
//    work(ProtoStructureWriter(tmpOut))
//    tmpOut.flush()
//    out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
    this
  
  override def putChoice(picklee: Any, tag: Choice[?], choice: String)(work: PickleWriter => Unit): PickleWriter =
    // TODO - For now we need to encode this as a NESTED structure at the current field value...
    // We need to figure out how to treat these as 'oneof' fields.
    val ordinal = tag.ordinal(picklee.asInstanceOf)
    val size = cache.cachedSize(picklee, streams.sizeOf(cache, work))
    out.writeInt(WireFormat.LengthDelimited.makeTag(fieldNum))
    out.writeInt(size)
    work(ProtoFieldWriter(out, ordinal+1))

//    val tmpByteOut = java.io.ByteArrayOutputStream()
//    val tmpOut = ProtoOutputStream(tmpByteOut)
//    work(ProtoFieldWriter(out, ordinal+1))
//    tmpOut.flush()
//    out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
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
    out.writeByteArray(fieldNum, cache.cachedUtf8(value))
    // out.writeString(fieldNum, value)
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class ProtoStructureWriter(out: ProtoOutputStream, cache: ProtoSerializationCache = SimpleProtoSerializationCache()) extends PickleStructureWriter:
  override def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    pickler(ProtoFieldWriter(out, number, cache))
    this

/** A pickle writer that will only write proto messages using ProtoTypeDescriptors. */
class ProtoWriter(
    out: ProtoOutputStream,
    cache: ProtoSerializationCache = SimpleProtoSerializationCache()
) extends PickleWriter with PickleCollectionWriter:
  override def putStructure(picklee: Any, tag: Struct[?])(work: PickleStructureWriter => Unit): PickleWriter =
    work(ProtoStructureWriter(out, cache))
    this

  // --------------------------------------------------------------------------
  // Note: Everything below here violates normal protocol buffer specification.
  // --------------------------------------------------------------------------
  override def putUnit(): PickleWriter = this
  override def putBoolean(value: Boolean): PickleWriter =
    out.writeBoolean(value)
    this
  override def putByte(value: Byte): PickleWriter =
    out.writeByte(value)
    this
  override def putChar(value: Char): PickleWriter =
    out.writeChar(value)
    this
  override def putShort(value: Short): PickleWriter =
    out.writeShort(value)
    this
  override def putInt(value: Int): PickleWriter =
    out.writeInt(value)
    this
  override def putLong(value: Long): PickleWriter =
    out.writeLong(value)
    this
  override def putFloat(value: Float): PickleWriter =
    out.writeFloat(value)
    this
  override def putDouble(value: Double): PickleWriter =
    out.writeDouble(value)
    this
  override def putString(value: String): PickleWriter =
    out.writeString(value)
    this
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    out.writeInt(length)
    work(this)
    this
  override def putElement(work: PickleWriter => Unit): PickleCollectionWriter =
    work(this)
    this
  override def putChoice(picklee: Any, tag: Choice[_], choice: String)(work: PickleWriter => Unit): PickleWriter =
    val ordinal = tag.asInstanceOf[Choice[_]].ordinal(picklee.asInstanceOf)
    work(ProtoFieldWriter(out, ordinal+1, cache))
    this
  override def flush(): Unit = out.flush()
