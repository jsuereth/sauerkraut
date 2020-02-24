package sauerkraut
package benchmarks

import core.{Writer,Buildable,given}
import format.pb.{RawBinary,Protos,TypeDescriptorMapping,field,given}
import format.json.{Json,given}
import format.nbt.{Nbt,given}
import org.openjdk.jmh.annotations._
import java.io.{File,FileInputStream,FileOutputStream,FileWriter}
import java.util.concurrent.TimeUnit


case class SimpleMessage(value: Int @field(2), message: String @field(1))
    derives Writer, Buildable, TypeDescriptorMapping

case class LargerMessage(
  messages: List[SimpleMessage] @field(1),
  otherNums: List[Double] @field(2),
  ints: List[Long] @field(3)
) derives Writer, Buildable, TypeDescriptorMapping

val EXAMPLE_INT=1124312542
val EXAMPLE_STRING="This is a test of simple byte serialization for us all"

@State(Scope.Thread)
@AuxCounters(AuxCounters.Type.EVENTS)
class FileSizeCounter
  var bytesWritten: Long = 0

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
abstract class JmhBenchmarks
  /** Abstract implementation of loading. */
  protected def load[T: Buildable](store: File): T
  protected def save[T: Writer](value: T, store: File): Unit

  private inline def withTempFile[T](use: File => T): T =
    val tmp = File.createTempFile("serialization", ".pickle")
    try use(tmp)
    finally tmp.delete()

  private inline def readAndWrite[T : Buildable : Writer](counter: FileSizeCounter)(value: T): Unit =
    withTempFile { file =>
      save(value, file)
      counter.bytesWritten = file.length
      load[T](file)
      ()
    }


  @Benchmark
  def writeAndReadSimpleMessageFromFile(counter: FileSizeCounter): Unit = 
    readAndWrite(counter)(SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING))

  @Benchmark
  def writeAndReadLargeNestedMessageFromFile(counter: FileSizeCounter): Unit =
    readAndWrite(counter)(LargerMessage(
      messages = List(
        SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING),
        SimpleMessage(0, ""),
        SimpleMessage(-1, "ANother string")),
      otherNums = List(1.0, -0.000001, 1000000000000000.0101010),
      ints = List(1,2,3,4,5,-1,-2,-4,1425,0)))

class JsonBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: File): T =
    pickle(Json).from(store).read[T]
  override def save[T: Writer](value: T, store: File): Unit =
    pickle(Json).to(FileWriter(store)).write(value)

class NbtBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: File): T =
    pickle(Nbt).from(new FileInputStream(store)).read[T]
  override def save[T: Writer](value: T, store: File): Unit =
    pickle(Nbt).to(new FileOutputStream(store)).write(value)

class RawBinaryBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: File): T =
    pickle(RawBinary).from(new FileInputStream(store)).read[T]
  override def save[T: Writer](value: T, store: File): Unit =
    pickle(RawBinary).to(new FileOutputStream(store)).write(value)

// class ProtoBinaryBenchmarks extends JmhBenchmarks
//   val MyProtos = Protos[SimpleMessage *: LargerMessage *: Unit]()
//   class RawBinaryBenchmarks extends JmhBenchmarks
//   override def load[T: Buildable](store: File): T =
//     pickle(MyProtos).from(new FileInputStream(store)).read[T]
//   override def save[T: Writer](value: T, store: File): Unit =
//     pickle(MyProtos).to(new FileOutputStream(store)).write(value)
  // TODO - compare to raw PB parsing...