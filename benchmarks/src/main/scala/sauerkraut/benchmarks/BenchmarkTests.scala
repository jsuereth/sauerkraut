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
package benchmarks

import core.{Writer,Buildable,given}
import java.nio.ByteBuffer
import java.io.OutputStreamWriter
import format.pb.{RawBinary,Protos,ProtoTypeDescriptor,field,given}
import format.json.{Json,given}
import format.nbt.{Nbt,given}
import format.xml.{Xml,given}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer


case class SimpleMessage(value: Int @field(2), message: String @field(1))
    derives Writer, Buildable, ProtoTypeDescriptor

case class LargerMessage(
  messages: ArrayBuffer[SimpleMessage] @field(1),
  otherNums: ArrayBuffer[Double] @field(2),
  ints: ArrayBuffer[Long] @field(3)
) derives Writer, Buildable, ProtoTypeDescriptor

val EXAMPLE_INT=1124312542
val EXAMPLE_STRING="This is a test of simple byte serialization for us all"

/** 
 * Benchmarking is done via config parms.  In setup, we selct and instance of
 * config to use to run the benchmark.
 */
trait BenchmarkConfig[T]:
  def name: String
  def message: T
  def load(store: ByteBuffer): T
  def save(value: T, store: ByteBuffer): Unit

abstract class SauerkrautBenchmarkConfig extends BenchmarkConfig[LargerMessage]:
  override def message: LargerMessage = LargerMessage(
      messages = ArrayBuffer(
        SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING),
        SimpleMessage(0, ""),
        SimpleMessage(-1, "ANother string")),
      otherNums = ArrayBuffer(1.0, -0.000001, 1000000000000000.0101010),
      ints = ArrayBuffer(1,2,3,4,5,-1,-2,-4,1425,0))

/** Configuration for how to run a protocol buffer benchmark. */
object ProtocolBufferBenchmarkConfig extends BenchmarkConfig[proto.Bench.LargerMessage]:
  override val name: String = "java_pb"
  override def message: proto.Bench.LargerMessage =
    proto.Bench.LargerMessage.newBuilder()
      .addMessages(
        proto.Bench.SimpleMessage.newBuilder()
        .setValue(EXAMPLE_INT)
        .setMessage(EXAMPLE_STRING))
      .addMessages(
        proto.Bench.SimpleMessage.newBuilder()
        .setValue(0)
        .setMessage(""))
      .addMessages(proto.Bench.SimpleMessage.newBuilder()
        .setValue(-1)
        .setMessage("ANother string"))
      .addOtherNums(1.0)
      .addOtherNums(-0.000001)
      .addOtherNums(1000000000000000.0101010)
      .addInts(1)
      .addInts(2)
      .addInts(3)
      .addInts(4)
      .addInts(5)
      .addInts(-1)
      .addInts(-2)
      .addInts(-4)
      .addInts(1425)
      .addInts(0)
      .build()
  override def load(store: ByteBuffer): proto.Bench.LargerMessage =
    proto.Bench.LargerMessage.parseFrom(store)
  override def save(value: proto.Bench.LargerMessage, store: ByteBuffer): Unit =
    value.writeTo(store.out)

/** Configuration for running XML format benchmarks. */
object SauerkrautXmlBenchmarkConfig extends SauerkrautBenchmarkConfig:
  override val name: String = "xml"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(Xml).from(store.in).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(Xml).to(store.writer).write(value)

/** Configuration for running XML format benchmarks. */
object SauerkrautJsonBenchmarkConfig extends SauerkrautBenchmarkConfig:
  override val name: String = "json"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(Json).from(store).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(Json).to(store.writer).write(value)

/** Configuration for running XML format benchmarks. */
object SauerkrautNbtBenchmarkConfig extends SauerkrautBenchmarkConfig:
  override val name: String = "nbt"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(Nbt).from(store.in).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(Nbt).to(store.out).write(value)

/** Configuration for running XML format benchmarks. */
object SauerkrautRawBinaryBenchmarkConfig extends SauerkrautBenchmarkConfig:
  override val name: String = "raw"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(RawBinary).from(store.in).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(RawBinary).to(store.out).write(value)

/** Configuration for running XML format benchmarks. */
object SauerkrautProtoBenchmarkConfig extends SauerkrautBenchmarkConfig:
  val MyProtos = Protos[SimpleMessage *: LargerMessage *: EmptyTuple]()
  override val name: String = "proto"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(MyProtos).from(store.in).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(MyProtos).to(store.out).write(value)

/** Configuration for running Java Serializaiton format. */
object JavaSerializationBenchmarks extends SauerkrautBenchmarkConfig:
  override val name: String = "java_ser"
  override def load(store: ByteBuffer): LargerMessage =
    java.io.ObjectInputStream(store.in).readObject.asInstanceOf
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    val out = java.io.ObjectOutputStream(store.out)
    out.writeObject(value)
    out.flush()


/** Helper to record the # of bytes written in a benchmark. */
@AuxCounters(AuxCounters.Type.EVENTS)
@State(Scope.Thread)
class BytesWritten:
  var bytesWritten: Int = 0

val benchmarkConfigs = Set(
  // Saurkraut Formats
  SauerkrautXmlBenchmarkConfig,
  SauerkrautJsonBenchmarkConfig,
  SauerkrautNbtBenchmarkConfig,
  SauerkrautRawBinaryBenchmarkConfig,
  SauerkrautProtoBenchmarkConfig,
  // Competitor frameworks
  JavaSerializationBenchmarks,
  ProtocolBufferBenchmarkConfig)

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class ReadBenchmarks:
  private var config: BenchmarkConfig[?] = null
  private var buffer = ByteBuffer.allocate(1024*1024)
  @Param(Array("raw", "proto", "nbt", "json", "xml", "java_pb", "java_ser"))
  var configName: String = null;
  @Setup(Level.Invocation) def setUp(): Unit =
    config = benchmarkConfigs.find(_.name == configName).get
    buffer.clear()
    config.save(config.message.asInstanceOf, buffer)
    buffer.flip()
  @Benchmark
  def read(bh: Blackhole): Unit =
    bh.consume(config.load(buffer))    
  
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class WriteBenchmarks:
  private var config: BenchmarkConfig[?] = null
  private var buffer = ByteBuffer.allocate(1024*1024)
  @Param(Array("raw", "proto", "nbt", "json", "xml", "java_pb", "java_ser"))
  var configName: String = null;
  @Setup(Level.Invocation) def setUp(): Unit =
    config = benchmarkConfigs.find(_.name == configName).get
    buffer.clear()
  @Benchmark
  def write(counter: BytesWritten, bh: Blackhole): Unit =
    config.save(config.message.asInstanceOf, buffer)
    buffer.flip()
    counter.bytesWritten = buffer.remaining


// /** Helper to pre-allocate a large enough byte buffer to run benchmarks. */
// @State(Scope.Thread)
// class Bytes:
//   // We allocate 1M for all serialization tests.
//   val buffer = ByteBuffer.allocate(1024*1024)
//   @Setup(Level.Invocation) def setUp(): Unit =
//     buffer.clear()

//   def flip(counter: BytesWritten): Unit =
//     buffer.flip()
//     counter.bytesWritten = buffer.remaining

// // TODO: Benchmark times are currently dominated by FILE I/O operations....
// //       IF we can simulate file I/O effectively, we could get better numbers.
// //       However, it IS true the JAWN has an advantage over any InputStream inputs due to its architecture.
// @State(Scope.Benchmark)
// @BenchmarkMode(Array(Mode.AverageTime))
// @OutputTimeUnit(TimeUnit.NANOSECONDS)
// abstract class JmhBenchmarks:
//   /** Abstract implementation of loading. */
//   protected def load[T: Buildable](store: ByteBuffer): T
//   protected def save[T: Writer](value: T, store: ByteBuffer): Unit

//   private inline def readAndWrite[T : Buildable : Writer](bytes: Bytes, counter: BytesWritten)(value: T): T =
//     save(value, bytes.buffer)
//     bytes.flip(counter)
//     load[T](bytes.buffer)
//   private def simpleMessage = SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING)
//   private def largeNestedMessage = LargerMessage(
//       messages = ArrayBuffer(
//         SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING),
//         SimpleMessage(0, ""),
//         SimpleMessage(-1, "ANother string")),
//       otherNums = ArrayBuffer(1.0, -0.000001, 1000000000000000.0101010),
//       ints = ArrayBuffer(1,2,3,4,5,-1,-2,-4,1425,0))

//   @Benchmark
//   def writeSimpleMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit = 
//     bh.consume(save(simpleMessage, bytes.buffer))
//   @Benchmark
//   def writeLargeNestedMessage(bytes: Bytes, bh: Blackhole): Unit =
//     bh.consume(save(largeNestedMessage, bytes.buffer))

//   // TODO - read-only benchmark

//   @Benchmark
//   def writeAndReadSimpleMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit = 
//     bh.consume(readAndWrite(bytes, counter)(simpleMessage))

//   @Benchmark
//   def writeAndReadLargeNestedMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit =
//     bh.consume(readAndWrite(bytes, counter)(largeNestedMessage))

// class XmlBenchmarks extends JmhBenchmarks:
//   override def load[T: Buildable](store: ByteBuffer): T =
//     pickle(Xml).from(store.in).read[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     pickle(Xml).to(store.writer).write(value)

// class JsonBenchmarks extends JmhBenchmarks:
//   override def load[T: Buildable](store: ByteBuffer): T =
//     pickle(Json).from(store).read[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     pickle(Json).to(store.writer).write(value)

// class NbtBenchmarks extends JmhBenchmarks:
//   override def load[T: Buildable](store: ByteBuffer): T =
//     pickle(Nbt).from(store.in).read[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     pickle(Nbt).to(store.out).write(value)

// class RawBinaryBenchmarks extends JmhBenchmarks:
//   override def load[T: Buildable](store: ByteBuffer): T =
//     pickle(RawBinary).from(store).read[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     pickle(RawBinary).to(store).write(value)

// class JavaSerializationBenchmarks extends JmhBenchmarks:
//   override def load[T: Buildable](store: ByteBuffer): T =
//     java.io.ObjectInputStream(store.in).readObject.asInstanceOf[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     val out = java.io.ObjectOutputStream(store.out)
//     out.writeObject(value)
//     out.flush()

// class SauerkrautProtocolBufferBenchmarks extends JmhBenchmarks:
//   val MyProtos = Protos[SimpleMessage *: LargerMessage *: EmptyTuple]()
//   override def load[T: Buildable](store: ByteBuffer): T =
//     pickle(MyProtos).from(store).read[T]
//   override def save[T: Writer](value: T, store: ByteBuffer): Unit =
//     pickle(MyProtos).to(store).write(value)


// @State(Scope.Benchmark)
// @BenchmarkMode(Array(Mode.AverageTime))
// @OutputTimeUnit(TimeUnit.NANOSECONDS)
// class JavaProtocolBufferBenchmarks:
//   import proto.Bench
//   private def simpleMessage = Bench.SimpleMessage.newBuilder()
//       .setValue(EXAMPLE_INT)
//       .setMessage(EXAMPLE_STRING)
//       .build()
//   @Benchmark
//   def writeAndReadSimpleMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit =      
//     val out = com.google.protobuf.CodedOutputStream.newInstance(bytes.buffer.out)
//     simpleMessage.writeTo(out)
//     out.flush()
//     bytes.flip(counter)
//     bh.consume(Bench.SimpleMessage.parseFrom(bytes.buffer))

//   private def largeNestedMessage = Bench.LargerMessage.newBuilder()
//       .addMessages(
//         Bench.SimpleMessage.newBuilder()
//         .setValue(EXAMPLE_INT)
//         .setMessage(EXAMPLE_STRING))
//       .addMessages(
//         Bench.SimpleMessage.newBuilder()
//         .setValue(0)
//         .setMessage(""))
//       .addMessages(Bench.SimpleMessage.newBuilder()
//         .setValue(-1)
//         .setMessage("ANother string"))
//       .addOtherNums(1.0)
//       .addOtherNums(-0.000001)
//       .addOtherNums(1000000000000000.0101010)
//       .addInts(1)
//       .addInts(2)
//       .addInts(3)
//       .addInts(4)
//       .addInts(5)
//       .addInts(-1)
//       .addInts(-2)
//       .addInts(-4)
//       .addInts(1425)
//       .addInts(0)
//       .build()

//   @Benchmark
//   def writeAndReadLargeNestedMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit =
//     // Note: We include canonical usage of builder pattern for construction
//     // of PB buffers.  This is to offset the simpler construction of Case classes
//     // in Scala given their canonical usage in Scala programs.
//     largeNestedMessage.writeTo(bytes.buffer.out)
//     bytes.flip(counter)
//     bh.consume(Bench.LargerMessage.parseFrom(bytes.buffer))
//   @Benchmark
//   def writeSimpleMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit = 
//     bh.consume(simpleMessage.writeTo(bytes.buffer.out))
//   @Benchmark
//   def writeLargeNestedMessage(bytes: Bytes, bh: Blackhole): Unit =
//     bh.consume(largeNestedMessage.writeTo(bytes.buffer.out))