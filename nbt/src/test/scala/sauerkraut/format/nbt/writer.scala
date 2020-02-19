package sauerkraut
package format
package nbt

import org.junit.Test
import org.junit.Assert._
import core.{Writer,given}

def hexString(buf: Array[Byte]): String =
  buf.map(b => f"$b%02x").mkString("")

case class Derived(x: Boolean, test: String)
  derives Writer

/** Ensure simple writing of nbts matches spec.
 * Complicated behaviors + reading is
 * tested via ComplianceTests.
 */
class TestWriter
  def nbtOf[T: Writer](value: T): Array[Byte] =
    val out = java.io.ByteArrayOutputStream()
    pickle(Nbt).to(out).write(value)
    out.toByteArray()
  def nbtStringOf[T: Writer](value: T): String =
    hexString(nbtOf(value))

  @Test def writeUnit(): Unit =
    assertEquals("", nbtStringOf(()))
  @Test def writeBoolean(): Unit =
    assertEquals("0101", nbtStringOf(true))
    assertEquals("0100", nbtStringOf(false))
  @Test def writeByte(): Unit =
    assertEquals("0108", nbtStringOf(8.toByte))
    assertEquals("01ff", nbtStringOf(255.toByte))
  @Test def writeChar(): Unit =
    assertEquals("020061", nbtStringOf('a'))
  @Test def writeShort(): Unit =
    assertEquals("020200", nbtStringOf(512.toShort))
  @Test def writeInt(): Unit =
    assertEquals("0300000010", nbtStringOf(16))
  @Test def writeLong(): Unit =
    assertEquals("040000000000000001", nbtStringOf(1L))
    assertEquals("04ffffffffffffffff", nbtStringOf(-1L))
  @Test def writeFloat(): Unit =
    assertEquals("053f800000", nbtStringOf(1.0f))
  @Test def writeDouble(): Unit =
    assertEquals("063ff0000000000000", nbtStringOf(1.0))
  @Test def writeString(): Unit =
    // [Tag] [Length] [Utf-8 String]
    assertEquals("08"+"0002"+"6162", nbtStringOf("ab"))
  @Test def writeStruct(): Unit =
    assertEquals(
        // [TagCompound] 
        //    [TagByte] [Length] [Name] [Byte]
        //    [TagString] [Length] [Name] [Length] [String]
        // [TagEnd]
        "0a" +
            "01" + "0001" + "78" + "01" +
            "08" + "0004" + "74657374" + "0002" + "6162" +
        "00",
    nbtStringOf(Derived(true, "ab")))