package sauerkraut
package format
package pb

import org.junit.Test
import org.junit.Assert._
import core.{Writer,given}

// Example from: https://developers.google.com/protocol-buffers/docs/encoding
// ```
// message Test1 {
//   optional int32 a = 1;
// }
// ```
case class Nesting(a: Int @field(1))
  derives Writer, TypeDescriptorMapping

// ```
// message Test3 {
//   optional Test1 c = 3;
// }
// ```
case class Nested(c: Nesting @field(3))
  derives Writer, TypeDescriptorMapping

val MyProtos = Protos[(Nested, Nesting)]()
      

class TestProtocolBufferWithDesc
  def hexString(buf: Array[Byte]): String =
    buf.map(b => f"$b%02x").mkString("")
  def binaryWithDesc[T: Writer : TypeDescriptorMapping](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    pickle(MyProtos).to(out).write(value)
    out.toByteArray()
  def binaryStringWithDesc[T : Writer : TypeDescriptorMapping](value: T): String =
    hexString(binaryWithDesc(value))

  @Test def writeNested(): Unit =
    assertEquals("089601", binaryStringWithDesc(Nesting(150)))
    assertEquals("1a03089601", binaryStringWithDesc(Nested(Nesting(150))))