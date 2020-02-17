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

trait SizeEstimator
  def finalSize: Int

class RawCollectionSizeEstimateWriter extends PickleCollectionWriter with SizeEstimator
  private var size: Int = 0
  override def finalSize: Int = size
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    // TODO - implement.  We're getting away with bogus data here...
    this

/** This is a pickle writer that just tries to recursively
 * guess the size of a sub message.
 * 
 * Note: we currently forget subsizes after we've computed
 * them.  That's a huge optimisation win if we can fix it.
 */
class FieldSizeEstimateWriter(fieldNum: Int,
  optDescriptor: Option[TypeDescriptorMapping[?]])
    extends PickleWriter
    with PickleCollectionWriter
    with SizeEstimator
  private var size: Int = 0
  override def finalSize: Int = size
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter =
    tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => 
        size += CodedOutputStream.computeBoolSize(
            fieldNum, 
            picklee.asInstanceOf)
      case PrimitiveTag.ByteTag => size += 1
      case PrimitiveTag.CharTag =>
        size += CodedOutputStream.computeInt32Size(
            fieldNum, picklee.asInstanceOf[Char].toInt)
      case PrimitiveTag.ShortTag =>
        size += CodedOutputStream.computeInt32Size(
            fieldNum, picklee.asInstanceOf[Short].toInt)
      case PrimitiveTag.IntTag =>
        size += CodedOutputStream.computeInt32Size(
            fieldNum, picklee.asInstanceOf[Int])
      case PrimitiveTag.LongTag =>
        size += CodedOutputStream.computeInt64Size(
            fieldNum, picklee.asInstanceOf[Long]
        )
      case PrimitiveTag.FloatTag =>
        size += CodedOutputStream.computeFloatSize(
            fieldNum, picklee.asInstanceOf[Float])
      case PrimitiveTag.DoubleTag =>
        size += CodedOutputStream.computeDoubleSize(
            fieldNum, picklee.asInstanceOf[Double])
      case PrimitiveTag.StringTag =>
        size += CodedOutputStream.computeStringSize(
            fieldNum, picklee.asInstanceOf[String])
    this
  // TODO - Primitives behave differently from messages...
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter = 
    work(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter = 
    val descriptor = optDescriptor match
      case Some(d) => d
      case None => RawBinaryTypeDescriptorMapping()
    val subSize =
      val tmp = SizeEstimateStructureWriter(descriptor)
      work(tmp)
      tmp.finalSize
    // Structure are written as follows:
    // [TAG] [SIZE] [RAW BYTES]
    size += CodedOutputStream.computeTagSize(fieldNum)
    size += CodedOutputStream.computeUInt32SizeNoTag(subSize)
    size += subSize
    this
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  def flush(): Unit = ()

/** Estimate the size of sub-structure given a TypeDescriptor. */
class SizeEstimateStructureWriter(d: TypeDescriptorMapping[?]) 
    extends PickleStructureWriter
    with SizeEstimator
  private var size = 0
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = d.fieldNumber(name)
    val fieldPickle = FieldSizeEstimateWriter(idx, d.fieldDescriptor(name))
    pickler(fieldPickle)
    size += fieldPickle.finalSize
    this
  override def finalSize = size