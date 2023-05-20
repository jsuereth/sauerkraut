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
import format.pb.{Proto, given}
import format.json.{Json,given}
import format.nbt.{Nbt,given}
import format.xml.{Xml,given}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer


// -- Saurkraut Classes --
case class SimpleMessage(value: Int @Field(2), message: String @Field(1))
    derives Writer, Buildable, upickle.default.ReadWriter
case class LargerMessage(
  messages: ArrayBuffer[SimpleMessage] @Field(1),
  otherNums: ArrayBuffer[Double] @Field(2),
  ints: ArrayBuffer[Long] @Field(3)
) derives Writer, Buildable, upickle.default.ReadWriter

// -- Java framework classes --
class JavaSimpleMessage {
  var value: Int = 0
  var message: String = ""
}
class JavaLargerMessage {
  var messages = ArrayBuffer[JavaSimpleMessage]()
  var otherNums = ArrayBuffer[Double]()
  var ints = ArrayBuffer[Long]()
}


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
object SauerkrautProtoBenchmarkConfig extends SauerkrautBenchmarkConfig:
  override val name: String = "proto"
  override def load(store: ByteBuffer): LargerMessage =
    pickle(Proto).from(store.in).read[LargerMessage]
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    pickle(Proto).to(store.out).write(value)

/** Configuration for running Java Serializaiton format. */
object JavaSerializationBenchmarks extends SauerkrautBenchmarkConfig:
  override val name: String = "java_ser"
  override def load(store: ByteBuffer): LargerMessage =
    java.io.ObjectInputStream(store.in).readObject.asInstanceOf
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    val out = java.io.ObjectOutputStream(store.out)
    out.writeObject(value)
    out.flush()

/** Uses UPickle -> msgpack serialization. */
object UPickleBinarySerializationBenchmarks extends SauerkrautBenchmarkConfig:
  override val name: String = "upickle_binary_msgpack"
  override def load(store: ByteBuffer): LargerMessage =
    upickle.default.readBinary[LargerMessage](store.in)
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    upickle.default.writeBinaryTo(value, store.out)

/** Uses UPickle -> json serialization. */
object UPickleJsonSerializationBenchmarks extends SauerkrautBenchmarkConfig:
  override val name: String = "upickle_json"
  override def load(store: ByteBuffer): LargerMessage =
    upickle.default.read[LargerMessage](ujson.Readable.fromByteBuffer(store))
  override def save(value: LargerMessage, store: ByteBuffer): Unit =
    upickle.default.writeJs(value).writeBytesTo(store.out)

object KryoSerializationBenchmarks extends BenchmarkConfig[JavaLargerMessage]:
  private val kryo = com.esotericsoftware.kryo.Kryo()
  kryo.setRegistrationRequired(false)
  override def message =
    val outer = JavaLargerMessage()
    outer.messages = ArrayBuffer({
      val tmp = JavaSimpleMessage()
      tmp.value = EXAMPLE_INT
      tmp.message = EXAMPLE_STRING
      tmp
    }, {
      val tmp = JavaSimpleMessage()
      tmp.value = 0
      tmp.message = ""
      tmp
    }, {
      val tmp = JavaSimpleMessage()
      tmp.value = -1
      tmp.message = "ANother string"
      tmp
    })
    outer.otherNums = ArrayBuffer(1.0, -0.000001, 1000000000000000.0101010)
    outer.ints = ArrayBuffer(1,2,3,4,5,-1,-2,-4,1425,0)
    outer
  override val name: String = "java_kryo"
  override def load(store: ByteBuffer): JavaLargerMessage =
    val in = com.esotericsoftware.kryo.io.Input(store.in)
    kryo.readObject(in, classOf[JavaLargerMessage])
  override def save(value: JavaLargerMessage, store: ByteBuffer): Unit =
    val out = com.esotericsoftware.kryo.io.Output(store.out)
    kryo.writeObject(out, value)
    out.close()

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
  SauerkrautProtoBenchmarkConfig,
  // Competitor frameworks
  UPickleBinarySerializationBenchmarks,
  UPickleJsonSerializationBenchmarks,
  JavaSerializationBenchmarks,
  ProtocolBufferBenchmarkConfig,
  KryoSerializationBenchmarks)

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.NANOSECONDS)
class ReadBenchmarks:
  private var config: BenchmarkConfig[?] = null
  private var buffer = ByteBuffer.allocate(1024*1024)
  // @Param(Array("proto", "nbt", "json", "xml", "java_pb", "java_ser", "java_kryo"))
  @Param(Array("proto", "json", "upickle_binary_msgpack", "upickle_json"))
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
  // @Param(Array("proto", "nbt", "json", "xml", "java_pb", "java_ser", "java_kryo"))
  @Param(Array("proto", "json", "upickle_binary_msgpack", "upickle_json"))
  var configName: String = null;
  @Setup(Level.Invocation) def setUp(): Unit =
    config = benchmarkConfigs.find(_.name == configName).get
    buffer.clear()
  @Benchmark
  def write(counter: BytesWritten, bh: Blackhole): Unit =
    bh.consume(config.save(config.message.asInstanceOf, buffer))
    buffer.flip()
    counter.bytesWritten = buffer.remaining
