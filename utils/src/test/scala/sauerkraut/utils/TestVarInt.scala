package sauerkraut
package utils

import com.google.protobuf.{CodedOutputStream, CodedInputStream}
import org.junit.Test
import org.junit.Assert._

def hexString(buf: Array[Byte]): String =
  buf.map(b => f"$b%02x").mkString("")


class TestVarInt:
  val buf = java.nio.ByteBuffer.allocate(32)

  
  def writeIntToHexString(value: Int): String =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    val length = buf.remaining()
    val nextBuf = new Array[Byte](length)
    buf.get(nextBuf)
    hexString(nextBuf)

  def writeLongToHexString(value: Long): String =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    val length = buf.remaining()
    val nextBuf = new Array[Byte](length)
    buf.get(nextBuf)
    hexString(nextBuf)

  def writeThenReadInt(value: Int): Int =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    VarInt.readInt(buf)

  def writeThenReadLong(value: Long): Long =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    VarInt.readLong(buf)

  def writeProtoThenReadLong(value: Long): Long =
    buf.clear()
    val out = CodedOutputStream.newInstance(buf)
    out.writeUInt64NoTag(value)
    out.flush()
    buf.flip()
    VarInt.readLong(buf)


  def writeProtoThenReadInt(value: Int): Int =
    buf.clear()
    val out = CodedOutputStream.newInstance(buf)
    out.writeUInt32NoTag(value)
    out.flush()
    buf.flip()
    VarInt.readInt(buf)

  def writeThenReadProtoInt(value: Int): Int =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    val in = CodedInputStream.newInstance(buf)
    in.readRawVarint32()

  def writeThenReadProtoLong(value: Long): Long =
    buf.clear()
    VarInt.write(value, buf)
    buf.flip()
    val in = CodedInputStream.newInstance(buf)
    in.readRawVarint64()

  @Test
  def testInt32Size(): Unit =
    assertEquals("Size of 1", 1, VarInt.byteSize(1))
    assertEquals("Size of 0", 1, VarInt.byteSize(0))
    assertEquals("Size of 256", 2, VarInt.byteSize(256))
    assertEquals("Size of 1048576", 3, VarInt.byteSize(1048576))
    assertEquals("Size of 111048576", 4, VarInt.byteSize(111048576))
    assertEquals("Size of Int.MaxValue", 5, VarInt.byteSize(Int.MaxValue))
    // Negative integers get converted to Uint64 for encoding
    assertEquals("Size of Int.MinValue", 10, VarInt.byteSize(Int.MinValue))
    assertEquals("Size of -1", 10, VarInt.byteSize(-1))

  @Test
  def testWriteInt32(): Unit =
    assertEquals("Writes value 1", "01", writeIntToHexString(1))
    assertEquals("Writes value 0", "00", writeIntToHexString(0))
    assertEquals("Writes value 256", "8002", writeIntToHexString(256))
    assertEquals("Writes value 1048576", "808040", writeIntToHexString(1048576))
    assertEquals("Writes value 111048576", "80eff934", writeIntToHexString(111048576))
    assertEquals("Writes value Int.MaxValue", "ffffffff07", writeIntToHexString(Int.MaxValue))
    // Negative integer values get converted into UInt64 for encoding.
    assertEquals("Writes value Int.MinValue", "80808080888080808000", writeIntToHexString(Int.MinValue))
    assertEquals("Writes value -1", "ffffffff8f8080808000", writeIntToHexString(-1))
  
  @Test
  def testInt64Size(): Unit =
    assertEquals("Size of 1", 1, VarInt.byteSize(1L))
    assertEquals("Size of 0", 1, VarInt.byteSize(0L))
    assertEquals("Size of 256", 2, VarInt.byteSize(256L))
    assertEquals("Size of 1048576", 3, VarInt.byteSize(1048576L))
    assertEquals("Size of 111048576", 4, VarInt.byteSize(111048576L))
    assertEquals("Size of Long.MaxValue", 9, VarInt.byteSize(Long.MaxValue))
    assertEquals("Size of Long.MinValue", 10, VarInt.byteSize(Long.MinValue))
    assertEquals("Size of -1", 10, VarInt.byteSize(-1L))

  @Test
  def testWriteInt64(): Unit =
    // Signed values encode the same as `Int`/Int32
    assertEquals("Writes value 1", "01", writeLongToHexString(1L))
    assertEquals("Writes value 0", "00", writeLongToHexString(0L))
    assertEquals("Writes value 256", "8002", writeLongToHexString(256L))
    assertEquals("Writes value 1048576", "808040", writeLongToHexString(1048576L))
    assertEquals("Writes value 111048576", "80eff934", writeLongToHexString(111048576L)) 
    // Is this correct encoding of negatives? we treat them as unsigned Long
    assertEquals("Writes value -1", "ffffffffffffffffff01", writeLongToHexString(-1L))
    assertEquals("Writes value Long.Max", "ffffffffffffffff7f", writeLongToHexString(Long.MaxValue))

  private val InterestingInts = Seq(1, 0, 256, 11048576, Int.MaxValue, -1, Int.MinValue)
  
  @Test def testWriteThenReadInt32(): Unit =
    for value <- InterestingInts
    do
      assertEquals(s"Reads a written int of $value", value, writeThenReadInt(value))

  @Test def compatibleWithProtoInt32(): Unit =
    for value <- InterestingInts
    do
      assertEquals(s"Reads a varint32 written by proto", value, writeProtoThenReadInt(value))
      assertEquals(s"Proto reads a varint32 written by us", value, writeThenReadProtoInt(value))

  private val InterestingLongs = Seq(1L, 0L, 256L, 11048576L, Long.MaxValue, -1L, Long.MinValue)

  @Test def testWriteThenReadInt64(): Unit =
    for value <- InterestingLongs
    do
      assertEquals(s"Reads a written long of $value", value, writeThenReadLong(value))
  
  @Test def compatibleWithProtoInt64(): Unit =
    for value <- InterestingLongs
    do
      assertEquals(s"Reads a varint32 written by proto", value, writeProtoThenReadLong(value))
      assertEquals(s"Proto reads a varint32 written by us", value, writeThenReadProtoLong(value))