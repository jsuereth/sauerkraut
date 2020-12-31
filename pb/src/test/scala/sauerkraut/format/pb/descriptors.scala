package sauerkraut
package format
package pb

import sauerkraut.{read,write}
import org.junit.Test
import org.junit.Assert._
import core.{Buildable,Writer,given}
import scala.collection.mutable.ArrayBuffer
import pretty.{Pretty, given}

// Example from: https://developers.google.com/protocol-buffers/docs/encoding
// ```
// message Test1 {
//   optional int32 a = 1;
// }
// ```
case class Nesting(a: Int @field(1))
  derives Buildable, Writer, ProtoTypeDescriptor

// ```
// message Test3 {
//   optional Test1 c = 3;
// }
// ```
case class Nested(c: Nesting @field(3))
  derives Buildable, Writer, ProtoTypeDescriptor


case class NestedCollections(
  messages: ArrayBuffer[Nesting] @field(1),
  otherNums: ArrayBuffer[Double] @field(2),
  ints: ArrayBuffer[Long] @field(3)
) derives Writer, Buildable, ProtoTypeDescriptor

val MyProtos = Protos[(Nested, Nesting, NestedCollections)]()
      


class TestProtocolBufferWithDesc:
  def hexString(buf: Array[Byte]): String =
    buf.map(b => f"$b%02x").mkString("")
  def binaryWithDesc[T: Writer : ProtoTypeDescriptor](value: T): Array[Byte] =    
    val out = java.io.ByteArrayOutputStream()    
    pickle(MyProtos).to(out).write(value)    
    out.toByteArray()
  def binaryStringWithDesc[T : Writer : ProtoTypeDescriptor](value: T): String =
    hexString(binaryWithDesc(value))

  def roundTrip[T : Buildable : Writer : ProtoTypeDescriptor](value: T): Unit =
    val out = java.io.ByteArrayOutputStream()
    pickle(MyProtos).to(out).write(value)
    val bytes = out.toByteArray()
    val in = java.io.ByteArrayInputStream(bytes)
    assertEquals(s"Failed to roundtrip", value, pickle(MyProtos).from(in).read[T])

  @Test def writeNested(): Unit =
    assertEquals("089601", binaryStringWithDesc(Nesting(150)))
    assertEquals("1a03089601", binaryStringWithDesc(Nested(Nesting(150))))
  @Test def roundTrip(): Unit =
    roundTrip(Nesting(150))
    roundTrip(Nested(Nesting(150)))
    roundTrip(NestedCollections(ArrayBuffer(Nesting(150)), ArrayBuffer(), ArrayBuffer()))
    roundTrip(NestedCollections(ArrayBuffer(Nesting(1)), ArrayBuffer(2.0, 3.0), ArrayBuffer(1L, 4L)))

  @Test def codegen(): Unit =
    summon[ProtoTypeDescriptor[NestedCollections]] match
      case msg: MessageProtoDescriptor[?] =>
        assertEquals("Failed to find correct field name", "messages", msg.fieldName(1))
        assertEquals("Failed to find correct field number", 1, msg.fieldNumber("messages"))
        assertEquals("Failed to find correct field descriptor for `messages: ArrayBuffer[Nesting] @field(1)`", fastTypeTag[ArrayBuffer[Nesting]](), msg.fieldDesc(1).tag)
        assertEquals("Failed to find correct field name", "otherNums", msg.fieldName(2))
        assertEquals("Failed to find correct field number", 2, msg.fieldNumber("otherNums"))
        assertEquals("Failed to find correct field descriptor for `otherNums: ArrayBuffer[Double] @field(2)`", fastTypeTag[ArrayBuffer[Double]](), msg.fieldDesc(2).tag)
        assertEquals("Failed to find correct field name", "ints", msg.fieldName(3))
        assertEquals("Failed to find correct field number", 3, msg.fieldNumber("ints"))
        assertEquals("Failed to find correct field descriptor for `ints: ArrayBuffer[Long] @field(3)`", fastTypeTag[ArrayBuffer[Long]](), msg.fieldDesc(3).tag)
      case other => fail(s"Expected to generated message proto descriptor, got: $other")