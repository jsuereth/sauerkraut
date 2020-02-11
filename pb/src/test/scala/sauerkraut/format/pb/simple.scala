package sauerkraut
package format
package pb

import org.junit.Test
import org.junit.Assert._
import core.{Writer,given}
import com.google.protobuf.CodedOutputStream


case class Derived(x: Boolean, test: String) derives Writer
case class Repeated(x: List[Boolean]) derives Writer

class TestProtocolBufferSimple
  def binary[T: Writer](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    val codedOut = CodedOutputStream.newInstance(out)
    val formatWriter = RawProtocolBufferPickleWriter(codedOut)
    summon[Writer[T]].write(value, formatWriter)
    codedOut.flush()
    out.toByteArray()
  def hexString(buf: Array[Byte]): String =
    buf.map(b => f"$b%02x").mkString("")
  def binaryString[T: Writer](value: T): String =
    hexString(binary(value))

  @Test def writeUnit(): Unit =
    assertEquals("", binaryString(()))
  @Test def writeBool(): Unit =
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

  @Test def writeDerived(): Unit =
    assertEquals("0800120774657374696e67", binaryString(Derived(false, "testing"))) 

  @Test def writeRepeated(): Unit =
    assertEquals("080008010800", binaryString(Repeated(List(false, true, false))))