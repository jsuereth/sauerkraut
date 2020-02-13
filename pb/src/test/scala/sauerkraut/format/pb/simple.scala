package sauerkraut
package format
package pb

import org.junit.Test
import org.junit.Assert._
import core.{Writer,given}
import com.google.protobuf.CodedOutputStream


case class Derived(x: Boolean, test: String) derives Writer
case class Repeated(x: List[Boolean]) derives Writer
// Example from: https://developers.google.com/protocol-buffers/docs/encoding
// ```
// message Test1 {
//   optional int32 a = 1;
// }
// ```
case class Nesting(a: Int) derives Writer
object NestingDesc extends TypeDescriptorMapping[Nesting]
  def fieldNumber(name: String): Int = name match
    case "a" => 1
    case _ => ???
  def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]] =
    None
given TypeDescriptorMapping[Nesting] = NestingDesc
// ```
// message Test3 {
//   optional Test1 c = 3;
// }
// ```
case class Nested(c: Nesting) derives Writer
object NestedDesc extends TypeDescriptorMapping[Nested]
  def fieldNumber(name: String) : Int = name match
    case "c" => 3
    case _   => ???
  def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]] =
    name match
      case "c" => Some(NestingDesc.asInstanceOf[TypeDescriptorMapping[F]])
      case _   => ???
given TypeDescriptorMapping[Nested] = NestedDesc

/** A collection of proto descriptors for our case classes. */
// TODO - find a way to autogenerate or simplify this..
object MyProtos extends Protos
  object repository extends TypeDescriptorRepository
    val NestedTag = fastTypeTag[Nested]()
    val NestingTag = fastTypeTag[Nesting]()
    def find[T](tag: FastTypeTag[T]): TypeDescriptorMapping[T] =
      tag match
        case NestedTag => NestedDesc.asInstanceOf[TypeDescriptorMapping[T]]
        case NestingTag => NestingDesc.asInstanceOf[TypeDescriptorMapping[T]]
        case _ => ???
      

class TestProtocolBufferSimple
  def binary[T: Writer](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    pickle(RawBinary).to(out).write(value)
    out.toByteArray()
  def hexString(buf: Array[Byte]): String =
    buf.map(b => f"$b%02x").mkString("")
  def binaryString[T: Writer](value: T): String =
    hexString(binary(value))

  def binaryWithDesc[T: Writer : TypeDescriptorMapping](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    pickle(MyProtos).to(out).write(value)
    val codedOut = CodedOutputStream.newInstance(out)
    out.toByteArray()
  def binaryStringWithDesc[T : Writer : TypeDescriptorMapping](value: T): String =
    hexString(binaryWithDesc(value))

  @Test def writeUnit(): Unit =
    assertEquals("", binaryString(()))
  @Test def writeBoolean(): Unit =
    assertEquals("01", binaryString(true))
    assertEquals("00", binaryString(false))
  @Test def writeChar(): Unit =
    assertEquals("21", binaryString('!'))
  @Test def writeShort(): Unit =
    assertEquals("01", binaryString(1.toShort))
  @Test def writeInt(): Unit =
    assertEquals("01", binaryString(1))
    assertEquals("ac02", binaryString(300))
  @Test def writeLong(): Unit =
    assertEquals("01", binaryString(1L))
    // TODO - something bigger than an INT
  @Test def writeFloat(): Unit =
    assertEquals("0000803f", binaryString(1.0f))
  @Test def writeDouble(): Unit =
    assertEquals("000000000000f03f", binaryString(1.0))
  @Test def writeString(): Unit =
    assertEquals("0774657374696e67", binaryString("testing"))
  // TODO - Ensure writing 'erased' as primitive throws.

  @Test def writeListOfInt(): Unit =
    assertEquals("020304", binaryString(List(3,4)))

  @Test def writeDerived(): Unit =
    assertEquals("0800120774657374696e67", binaryString(Derived(false, "testing"))) 

  @Test def writeRepeated(): Unit =
    assertEquals("080008010800", binaryString(Repeated(List(false, true, false))))

  @Test def writeNested(): Unit =
    assertEquals("089601", binaryStringWithDesc(Nesting(150)))
    assertEquals("1a03089601", binaryStringWithDesc(Nested(Nesting(150))))