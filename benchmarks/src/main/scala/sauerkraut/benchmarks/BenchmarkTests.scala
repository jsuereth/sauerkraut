package sauerkraut
package benchmarks

import core.{Writer,Buildable,given}
import java.nio.ByteBuffer
import java.io.OutputStreamWriter
import format.pb.{RawBinary,Protos,TypeDescriptorMapping,field,given}
import format.json.{Json,given}
import format.nbt.{Nbt,given}
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import scala.collection.mutable.ArrayBuffer


case class SimpleMessage(value: Int @field(2), message: String @field(1))
    derives Writer, Buildable, TypeDescriptorMapping

case class LargerMessage(
  messages: ArrayBuffer[SimpleMessage] @field(1),
  otherNums: ArrayBuffer[Double] @field(2),
  ints: ArrayBuffer[Long] @field(3)
) derives Writer, Buildable, TypeDescriptorMapping

val EXAMPLE_INT=1124312542
val EXAMPLE_STRING="This is a test of simple byte serialization for us all"

@State(Scope.Thread)
class Bytes
  // We allocate 1M for all serialization tests.
  val buffer = ByteBuffer.allocate(1024*1024)
  @Setup(Level.Invocation) def setUp(): Unit =
    buffer.clear()

  def flip(counter: BytesWritten): Unit =
    buffer.flip()
    counter.bytesWritten = buffer.remaining


@AuxCounters(AuxCounters.Type.EVENTS)
@State(Scope.Thread)
class BytesWritten
  var bytesWritten: Int = 0

// TODO: Benchmark times are currently dominated by FILE I/O operations....
//       IF we can simulate file I/O effectively, we could get better numbers.
//       However, it IS true the JAWN has an advantage over any InputStream inputs due to its architecture.
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
abstract class JmhBenchmarks
  /** Abstract implementation of loading. */
  protected def load[T: Buildable](store: ByteBuffer): T
  protected def save[T: Writer](value: T, store: ByteBuffer): Unit

  private inline def readAndWrite[T : Buildable : Writer](bytes: Bytes, counter: BytesWritten)(value: T): T =
    save(value, bytes.buffer)
    bytes.flip(counter)
    load[T](bytes.buffer)


  @Benchmark
  def writeAndReadSimpleMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit = 
    bh.consume(readAndWrite(bytes, counter)(SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING)))

  @Benchmark
  def writeAndReadLargeNestedMessage(bytes: Bytes, counter: BytesWritten, bh: Blackhole): Unit =
    bh.consume(readAndWrite(bytes, counter)(LargerMessage(
      messages = ArrayBuffer(
        SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING),
        SimpleMessage(0, ""),
        SimpleMessage(-1, "ANother string")),
      otherNums = ArrayBuffer(1.0, -0.000001, 1000000000000000.0101010),
      ints = ArrayBuffer(1,2,3,4,5,-1,-2,-4,1425,0))))

class JsonBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: ByteBuffer): T =
    pickle(Json).from(store).read[T]
  override def save[T: Writer](value: T, store: ByteBuffer): Unit =
    pickle(Json).to(store.writer).write(value)

class NbtBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: ByteBuffer): T =
    pickle(Nbt).from(store.in).read[T]
  override def save[T: Writer](value: T, store: ByteBuffer): Unit =
    pickle(Nbt).to(store.out).write(value)

class RawBinaryBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: ByteBuffer): T =
    pickle(RawBinary).from(store.in).read[T]
  override def save[T: Writer](value: T, store: ByteBuffer): Unit =
    pickle(RawBinary).to(store.out).write(value)

// class ProtoBinaryBenchmarks extends JmhBenchmarks
//   val MyProtos = Protos[SimpleMessage *: LargerMessage *: Unit]()
//   class RawBinaryBenchmarks extends JmhBenchmarks
//   override def load[T: Buildable](store: File): T =
//     pickle(MyProtos).from(new FileInputStream(store)).read[T]
//   override def save[T: Writer](value: T, store: File): Unit =
//     pickle(MyProtos).to(new FileOutputStream(store)).write(value)
  // TODO - compare to raw PB parsing...