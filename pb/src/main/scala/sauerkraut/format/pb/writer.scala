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
    desc: ProtoTypeDescriptor[?]) 
    extends PickleWriter with PickleCollectionWriter:
  // Writing a collection should simple write a field multiple times.
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    // This does NOT work for primitive collections.
    work(this)
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    desc match
      case msg: MessageProtoDescriptor[_] =>
        // We need to write a header for this structure proto, which includes its size.
        // For now, we be lazy and write to temporary array, then do it all at once.
        // TODO - figure out if we can precompute and do this faster!
        val tmpByteOut = java.io.ByteArrayOutputStream()
        val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
        work(DescriptorBasedProtoStructureWriter(tmpOut, msg))
        tmpOut.flush()
        out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
      // TODO - We the following in benchmarks:
      /* 
[info] java.lang.RuntimeException: Cannot find structure definition from: CollectionTypeDescriptor(Given(scala.collection.mutable.ArrayBuffer[scala.Long]),PrimitiveTypeDescriptor(LongTag))
[info]  at sauerkraut.format.pb.ProtocolBufferFieldWriter.putStructure(writer.scala:44)
[info]  at sauerkraut.benchmarks.SimpleMessage$$anon$1.write(BenchmarkTests.scala:34)
[info]  at sauerkraut.benchmarks.SimpleMessage$$anon$1.write(BenchmarkTests.scala:34)
[info]  at sauerkraut.core.CollectionWriter.write$$anonfun$2$$anonfun$1$$anonfun$1(collections.scala:32)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:15)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:10)
[info]  at sauerkraut.format.pb.ProtocolBufferFieldWriter.putElement(writer.scala:62)
[info]  at sauerkraut.core.CollectionWriter.write$$anonfun$3$$anonfun$2(collections.scala:32)
[info]  at scala.collection.IterableOnceOps.foreach(IterableOnce.scala:553)
[info]  at scala.collection.IterableOnceOps.foreach$(IterableOnce.scala:551)
[info]  at scala.collection.AbstractIterable.foreach(Iterable.scala:920)
[info]  at sauerkraut.core.CollectionWriter.write$$anonfun$1(collections.scala:32)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:15)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:10)
[info]  at sauerkraut.format.pb.ProtocolBufferFieldWriter.putCollection(writer.scala:31)
[info]  at sauerkraut.core.CollectionWriter.write(collections.scala:32)
[info]  at sauerkraut.core.CollectionWriter.write(collections.scala:29)
[info]  at sauerkraut.benchmarks.LargerMessage$.write$$anonfun$1$$anonfun$1(BenchmarkTests.scala:40)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:15)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:10)
[info]  at sauerkraut.format.pb.DescriptorBasedProtoStructureWriter.putField(writer.scala:74)
[info]  at sauerkraut.benchmarks.LargerMessage$.sauerkraut$benchmarks$LargerMessage$$anon$1$$_$write$$anonfun$4(BenchmarkTests.scala:40)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:15)
[info]  at dotty.runtime.function.JProcedure1.apply(JProcedure1.java:10)
[info]  at sauerkraut.format.pb.DescriptorBasedProtoWriter.putStructure(writer.scala:85)
[info]  at sauerkraut.benchmarks.LargerMessage$$anon$1.write(BenchmarkTests.scala:40)
[info]  at sauerkraut.benchmarks.LargerMessage$$anon$1.write(BenchmarkTests.scala:40)
[info]  at sauerkraut.api$package$.write(api.scala:49)
[info]  at sauerkraut.benchmarks.SauerkrautProtocolBufferBenchmarks.save(BenchmarkTests.scala:130)
[info]  at sauerkraut.benchmarks.JmhBenchmarks.inline$save(BenchmarkTests.scala:71)
[info]  at sauerkraut.benchmarks.JmhBenchmarks.writeAndReadLargeNestedMessage(BenchmarkTests.scala:91)
      */
      case _ => throw RuntimeException(s"Cannot find structure definition from: $desc")
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
    mapping: MessageProtoDescriptor[?]) extends PickleStructureWriter:
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = mapping.fieldNumber(name)
    pickler(ProtocolBufferFieldWriter(out, idx, mapping.fieldDesc(idx)))
    this

/** A pickle writer that will only write proto messages using ProtoTypeDescriptors. */
class DescriptorBasedProtoWriter(
    out: CodedOutputStream,
    repository: TypeDescriptorRepository
) extends PickleWriter:
  override def putStructure(picklee: Any, tag: FastTypeTag[?])(work: PickleStructureWriter => Unit): PickleWriter =
    repository.find(tag) match
      case msg: MessageProtoDescriptor[_] =>
        work(DescriptorBasedProtoStructureWriter(out, msg))
      case other =>
        throw WriteException(s"Unable to find message descriptor for $tag, found $other", null)
    this
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[?]): PickleWriter = ???
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter = ???
  override def flush(): Unit = out.flush()
