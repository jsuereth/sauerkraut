package sauerkraut
package format
package pb

import org.junit.Test
import org.junit.Assert._
import core.{Writer,given}

case class Derived(x: Boolean, test: String) derives Writer
case class Repeated(x: List[String]) derives Writer
case class RepeatedCompressed(x: List[Boolean]) derives Writer
enum SimpleEnum derives Writer:
  case One
  case Two

class TestProtoWriter:
  def binary[T: Writer](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    pickle(Proto).to(out).write(value)
    out.toByteArray()
  def binaryString[T: Writer](value: T): String =
    hexString(binary(value))

  // Test all non-proto writing functionality
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

  @Test def writeRepeatedCompressed(): Unit =
    assertEquals("0a03000100", binaryString(RepeatedCompressed(List(false, true, false))))

  @Test def writeRepeated(): Unit =
    assertEquals("0a06012101210121", binaryString(Repeated(List("!", "!", "!"))))
    ()

  @Test def writeEnum(): Unit =
    // Expect just field and then 0-length vaue.
    assertEquals("0a00", binaryString(SimpleEnum.One))
    assertEquals("1200", binaryString(SimpleEnum.Two))

  // test actual protocol buffer operations
  @Test def writeDerived(): Unit =
    assertEquals("0800120774657374696e67", binaryString(Derived(false, "testing"))) 