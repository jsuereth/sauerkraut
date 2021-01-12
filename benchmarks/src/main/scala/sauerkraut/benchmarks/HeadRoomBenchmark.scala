/*
 * Copyright 2020 Google
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

package sauerkraut.benchmarks
package headroom

import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

// Here we define a benchmark where we "optimise" (hand-write) various codegen techniques against API specifications to see which
// has the most ideal runtime performance.

// Option #1 - Current Design

// Option #2 - New PickleWriter that uses `putFieldX(number, name, <object | primitve>)`, no collections.


// For testing, we'll only use Float, Long + String primtives.


case class ExampleMessage(
  id: Long,
  name: String,
  helpers: collection.mutable.ArrayBuffer[OtherMessage]
)
case class OtherMessage(id: Long, name: String, value: Float)

// Existing solution
package option1 {
  import sauerkraut.{_,given}
  import sauerkraut.core.{_,given}
  import format.nbt.{Nbt,given}
  given sauerkraut.core.Writer[OtherMessage] = sauerkraut.core.Writer.derived[OtherMessage]
  given sauerkraut.core.Writer[ExampleMessage] = sauerkraut.core.Writer.derived[ExampleMessage]

  def write(value: ExampleMessage, store: ByteBuffer): Unit =
    pickle(Nbt).to(store.out).write(value)
}

// Everything looks like protobuf
package option2 {
  package core {
    trait Writer[T]:
      def write(value: T, pickle: format.PickleWriter): Unit

    trait Builder[T]:
      def putField[F](number: Int): Builder[F]
      def putField[F](name: String): Builder[F]
      def putLongField(number: Int, value: Long): Unit
      def putLongField(name: String, value: Long): Unit
      def putFloatField(number: Int, value: Float): Unit
      def putFloatField(name: String, value: Float): Unit
      def putStringField(number: Int, value: String): Unit
      def putStringField(name: String, value: String): Unit
      def result: T
  }
  package format {
    trait PickleWriter:
      def writeField[T: core.Writer](number: Int, name: String, value: T): Unit
      def writeLongField(number: Int, name: String, value: Long): Unit
      def writeFloatField(number: Int, name: String, value: Float): Unit
      def writeStringField(number: Int, name: String, value: String): Unit

    trait PickleReader:
      final def build[T: core.Builder]: T = push(summon[core.Builder[T]]).result
      def push[T](builder: core.Builder[T]): core.Builder[T]

    package nbt {
      import sauerkraut.format.nbt.internal.{NbtTag, TagOutputStream}
      class NbtPickleWriter(out: TagOutputStream) extends PickleWriter:
        def writeLongField(number: Int, name: String, value: Long): Unit =
          out.writeRawTag(NbtTag.TagLong)
          out.writeStringPayload(name)
          out.writeLongPayload(value)
        def writeFloatField(number: Int, name: String, value: Float): Unit =
          out.writeRawTag(NbtTag.TagFloat)
          out.writeStringPayload(name)
          out.writeFloatPayload(value)
        def writeStringField(number: Int, name: String, value: String): Unit =
          out.writeRawTag(NbtTag.TagString)
          out.writeStringPayload(name)
          out.writeStringPayload(value)
        def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
          out.writeRawTag(NbtTag.TagCompound)
          out.writeStringPayload(name)
          summon[core.Writer[T]].write(value, this)
          out.writeRawTag(NbtTag.TagEnd)
    }
    package proto {
      import com.google.protobuf.{CodedOutputStream, WireFormat}
      // We optimise string writing by pulling UTF-8 bytes directly.
      val UTF_8 = java.nio.charset.Charset.forName("UTF-8");
      class ProtoSizeEstimator extends PickleWriter:
        var size = 0
        def writeLongField(number: Int, name: String, value: Long): Unit =
          size += CodedOutputStream.computeInt64Size(number, value)
        def writeFloatField(number: Int, name: String, value: Float): Unit =
          size += CodedOutputStream.computeFloatSize(number, value)
        def writeStringField(number: Int, name: String, value: String): Unit =
          size += CodedOutputStream.computeByteArraySize(number, value.getBytes(UTF_8))
          // size += CodedOutputStream.computeStringSize(number, value)
        def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
          // Nested fun
          size += CodedOutputStream.computeTagSize(number)
          val nested = ProtoSizeEstimator()
          summon[core.Writer[T]].write(value, nested)
          size += CodedOutputStream.computeInt32SizeNoTag(nested.size) + nested.size
      class ProtoStructureWriter(out: CodedOutputStream) extends PickleWriter:
        def writeLongField(number: Int, name: String, value: Long): Unit =
          out.writeInt64(number, value)
        def writeFloatField(number: Int, name: String, value: Float): Unit =
          out.writeFloat(number, value)
        def writeStringField(number: Int, name: String, value: String): Unit =
          out.writeByteArray(number, value.getBytes(UTF_8))
        def writeField[T: core.Writer](number: Int, name: String, value: T): Unit =
          val writer = summon[core.Writer[T]]
          val sizeEstimator = ProtoSizeEstimator()
          writer.write(value, sizeEstimator)
          out.writeTag(number, WireFormat.WIRETYPE_LENGTH_DELIMITED)
          // Cheat for benchmark test on size...
          out.writeInt32NoTag(sizeEstimator.size)
          writer.write(value, this)
    }
  }

  given core.Writer[OtherMessage] with
    override def write(value: OtherMessage, pickle: format.PickleWriter): Unit =
      pickle.writeLongField(1, "id", value.id)
      pickle.writeStringField(2, "name", value.name)
      pickle.writeFloatField(3, "value", value.value)
  object ExampleMessageWriter extends core.Writer[ExampleMessage]:
    override def write(value: ExampleMessage, pickle: format.PickleWriter): Unit =
      pickle.writeLongField(1, "id", value.id)
      pickle.writeStringField(2, "name", value.name)
      var i = 0
      while (i < value.helpers.length)
        pickle.writeField(3, "helpers", value.helpers(i))
        i += 1


  def write(value: ExampleMessage, store: ByteBuffer): Unit =
    val out = sauerkraut.format.nbt.internal.TagOutputStream(java.io.DataOutputStream(store.out))
    val pickler = format.nbt.NbtPickleWriter(out)
    ExampleMessageWriter.write(value, pickler)
    out.flush()
  def writeProto(value: ExampleMessage, store: ByteBuffer): Unit =
    val out =  com.google.protobuf.CodedOutputStream.newInstance(store.out, 25)
    ExampleMessageWriter.write(value, format.proto.ProtoStructureWriter(out))
    out.flush()

}


package option3 {
  package format {
    trait PickleWriter:
      def todo(): Unit
  }
}

import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class WriteBenchmarks:
  private val kryo = com.esotericsoftware.kryo.Kryo()
  kryo.setRegistrationRequired(false)
  private var buffer = ByteBuffer.allocate(1024*1024)
  @Setup(Level.Invocation) def cleanBuffer(): Unit =
    buffer.clear()
  def message = ExampleMessage(1, "outer", collection.mutable.ArrayBuffer(
    OtherMessage(1, "first", 1.24),
    // OtherMessage(2, "second", -1),
    // OtherMessage(5, "A really really long string", 82.124515e1),
    // OtherMessage(9, "duplicated", 1e24),
    // OtherMessage(10000, "duplicated", -1e4),
    // OtherMessage(24, "something else", 0),
  ))
  def protoMessage = 
    val tmp = sauerkraut.benchmarks.headroom.proto.Headroom.ExampleMessage.newBuilder
      .setId(1)
      .setName("outer")
    tmp.addHelpersBuilder()
      .setId(1)
      .setName("first")
      .setValue(1.24)
    tmp.build()
  @Benchmark
  def writeOption1(bh: Blackhole): Unit =
    bh.consume(option1.write(message, buffer))
  @Benchmark
  def writeOption2(bh: Blackhole): Unit =
    bh.consume(option2.write(message, buffer))
  @Benchmark
  def writeOption2Proto(bh: Blackhole): Unit =
    bh.consume(option2.writeProto(message, buffer))
  @Benchmark
  def writeKryo(bh: Blackhole): Unit =
    val out = com.esotericsoftware.kryo.io.Output(buffer.out)
    bh.consume(kryo.writeObject(out, message))
    out.close()
  @Benchmark
  def writeProto(bh: Blackhole): Unit = bh.consume(writeProtoImpl())
  def writeProtoImpl(): Unit =
    val out = buffer.out
    protoMessage.writeTo(out)
    out.flush()
    out.close()