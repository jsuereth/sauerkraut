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
package streams

import java.nio.charset.{Charset, StandardCharsets}

def sizeOfStruct(cache: ProtoSerializationCache, work: PickleStructureWriter => Unit): Int =
  val sizeEstimator = ProtoSizeWriter(cache)
  work(sizeEstimator)
  sizeEstimator.finalSize

def sizeOf(cache: ProtoSerializationCache, work: PickleWriter => Unit): Int =
  val sizeEstimator = ProtoSizeWriter(cache)
  work(sizeEstimator)
  sizeEstimator.finalSize

// A pickler that just calculate the final size of a proto that will be generated.
class ProtoSizeWriter(cache: ProtoSerializationCache) extends PickleWriter
  with PickleCollectionWriter
  with PickleStructureWriter:
  private var size = 0
  private var fieldNum = 0 // Keeps trac of current fieldNum
  // TODO - size cache?
  override def putUnit(): PickleWriter =
    this
  override def putBoolean(value: Boolean): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putByte(value: Byte): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putChar(value: Char): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putShort(value: Short): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putInt(value: Int): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putLong(value: Long): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putFloat(value: Float): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putDouble(value: Double): PickleWriter =
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, value)
    else
      size += ProtoWireSize.sizeOf(value)
    this
  override def putString(value: String): PickleWriter =
    val prevSize = size
    val bytes = cache.cachedUtf8(value)
    if fieldNum > 0 then
      size += ProtoWireSize.sizeOf(fieldNum, bytes)
    else
      size += ProtoWireSize.sizeOf(bytes)
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - we could try to "cache" sizes at this layer.
    val prev = fieldNum
    fieldNum = 0
    pickler(this)
    fieldNum = prev
    this
  override def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val prev = fieldNum
    fieldNum = number
    pickler(this)
    fieldNum = prev
    this

  override def putCollection(length: Int, tag: CollectionTag[_, _])(work: PickleCollectionWriter => Unit): PickleWriter =
    tag.elementTag match
      case p: PrimitiveTag[_] if length > 1 =>
        // Primitive collections are written in compressed format.
        size += ProtoWireSize.sizeOfTag(WireFormat.LengthDelimited, fieldNum)
        // length-delimited needs to include the written collection length.
        var currentSize = size
        val prev = fieldNum
        fieldNum = 0
        work(this)
        fieldNum = prev
        val colSize: Int = size - currentSize
        size += ProtoWireSize.sizeOf(colSize)
      case elemTag => work(this)
    this


  override def putChoice(picklee: Any, tag: Choice[?], choice: String)(work: PickleWriter => Unit): PickleWriter =
    val ordinal = tag.asInstanceOf[Choice[_]].ordinal(picklee.asInstanceOf)
    val prev = fieldNum
    fieldNum = ordinal
    work(this)
    fieldNum = prev
    this

  override def putStructure(picklee: Any, tag: Struct[?])(work: PickleStructureWriter => Unit): PickleWriter =
    cache.getCachedSize(picklee) match
      case Some(value) =>
        size += size
      case None =>
        if fieldNum > 0 then
          size += ProtoWireSize.sizeOfTag(WireFormat.LengthDelimited, fieldNum)
        work(this)
    this

  override def flush(): Unit = ()

  final def finalSize = size