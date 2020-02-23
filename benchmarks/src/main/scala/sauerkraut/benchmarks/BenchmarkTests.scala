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

val EXAMPLE_INT=1124312542
val EXAMPLE_STRING="This is a test of simple byte serialization for us all"

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
abstract class JmhBenchmarks
  /** Abstract implementation of loading. */
  protected def load[T: Buildable](store: File): T
  protected def save[T: Writer](value: T, store: File): Unit

  private def withTempFile[T](use: File => T): T =
    val tmp = File.createTempFile("serialization", ".pickle")
    try use(tmp)
    finally tmp.delete()

  @Benchmark
  def writeAndReadSimpleMessageFromFile(): Unit = 
    withTempFile { file =>
      save(SimpleMessage(EXAMPLE_INT, EXAMPLE_STRING), file)
      load[SimpleMessage](file)
      ()
    }

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

class ProtoBinaryBenchmarks extends JmhBenchmarks
  val MyProtos = Protos[SimpleMessage *: Unit]()
  class RawBinaryBenchmarks extends JmhBenchmarks
  override def load[T: Buildable](store: File): T =
    pickle(MyProtos).from(new FileInputStream(store)).read[T]
  override def save[T: Writer](value: T, store: File): Unit =
    pickle(MyProtos).to(new FileOutputStream(store)).write(value)
  // TODO - compare to raw PB parsing...