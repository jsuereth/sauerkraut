package sauerkraut
package utils

import org.junit.{Test, Assert}

class OutputHelper(output: Array[Byte]):
  var position: Int = 0
  inline def writeByte(byte: Byte): Unit =
    output(position) = byte
    position += 1
  def writeBoolean(value: Boolean): Unit =
    InlineWriter.writeBoolean(value, (x) => writeByte(x))
  def writeShortBE(value: Short): Unit =
    InlineWriter.writeFixed16(value, (x) => writeByte(x), InlineWriter.Endian.Big)
  def writeShortLE(value: Short): Unit =
    InlineWriter.writeFixed16(value, (x) => writeByte(x), InlineWriter.Endian.Little)
  def writeIntBE(value: Int): Unit =
    InlineWriter.writeFixed32(value, (x) => writeByte(x), InlineWriter.Endian.Big)
  def writeIntLE(value: Int): Unit =
    InlineWriter.writeFixed32(value, (x) => writeByte(x), InlineWriter.Endian.Little)
  def writeLongBE(value: Long): Unit =
    InlineWriter.writeFixed64(value, (x) => writeByte(x), InlineWriter.Endian.Big)
  def writeLongLE(value: Long): Unit =
    InlineWriter.writeFixed64(value, (x) => writeByte(x), InlineWriter.Endian.Little)
  def writeFloatBE(value: Float): Unit =
    InlineWriter.writeFloat(value, (x) => writeByte(x), InlineWriter.Endian.Big)
  def writeFloatLE(value: Float): Unit =
    InlineWriter.writeFloat(value, (x) => writeByte(x), InlineWriter.Endian.Little)
  def writeDoubleBE(value: Double): Unit =
    InlineWriter.writeDouble(value, (x) => writeByte(x), InlineWriter.Endian.Big)
  def writeDoubleLE(value: Double): Unit =
    InlineWriter.writeDouble(value, (x) => writeByte(x), InlineWriter.Endian.Little)
  def writeStringUtf8(value: String): Unit =
    InlineWriter.writeStringUtf8(value, (x: Array[Byte]) => System.arraycopy(x, 0, output, position, x.length))
  

class InputHelper(input: Array[Byte]):
  var position: Int = 0
  inline def readByte(): Int =
    val result = input(position)
    position += 1
    result

  def readStringUtf8(length: Int): String =
    InlineReader.readStringUtf8(length, () => readByte())

class TestInlineUtils:
  @Test def testWriteBoolean(): Unit =
    val buf = new Array[Byte](2)
    OutputHelper(buf).writeBoolean(true)
    Assert.assertEquals(Seq[Byte](1, 0), buf.toSeq)
    OutputHelper(buf).writeBoolean(false)
    Assert.assertEquals(Seq[Byte](0, 0), buf.toSeq)

  @Test def testWriteShort(): Unit = 
    val buf = new Array[Byte](2)
    OutputHelper(buf).writeShortBE(1)
    Assert.assertEquals(Seq[Byte](0, 1), buf.toSeq)
    OutputHelper(buf).writeShortLE(1)
    Assert.assertEquals(Seq[Byte](1, 0), buf.toSeq)

  @Test def testWriteInt(): Unit =
    val buf = new Array[Byte](4)
    OutputHelper(buf).writeIntBE(1)
    Assert.assertEquals(Seq[Byte](0, 0, 0, 1), buf.toSeq)
    OutputHelper(buf).writeIntLE(1)
    Assert.assertEquals(Seq[Byte](1, 0, 0, 0), buf.toSeq)

  @Test def testWriteLong(): Unit =
    val buf = new Array[Byte](8)
    OutputHelper(buf).writeLongBE(1)
    Assert.assertEquals(Seq[Byte](0, 0, 0, 0, 0, 0, 0, 1), buf.toSeq)
    OutputHelper(buf).writeLongLE(1)
    Assert.assertEquals(Seq[Byte](1, 0, 0, 0, 0, 0, 0, 0), buf.toSeq)

  @Test def testWriteFloat(): Unit =
    val buf = new Array[Byte](4)
    // TODO - write spec for IEEE floating point layout here...
    OutputHelper(buf).writeFloatBE(1.0)
    Assert.assertEquals(Seq[Byte](63, -128, 0, 0), buf.toSeq)
    OutputHelper(buf).writeFloatBE(-2.123e24)
    Assert.assertEquals(Seq[Byte](-25, -32, -56, 8), buf.toSeq)
    OutputHelper(buf).writeFloatLE(1.0)
    Assert.assertEquals(Seq[Byte](0, 0, -128, 63), buf.toSeq)
    OutputHelper(buf).writeFloatLE(-2.123e24)
    Assert.assertEquals(Seq[Byte](8, -56, -32, -25), buf.toSeq)

  @Test def testWriteDouble(): Unit =
    val buf = new Array[Byte](8)
    OutputHelper(buf).writeDoubleBE(1.0)
    // TODO - write spec for IEEE floating point layout here...
    Assert.assertEquals(Seq[Byte](63, -16, 0, 0, 0, 0, 0, 0), buf.toSeq)
    OutputHelper(buf).writeDoubleBE(-2.123e24)
    Assert.assertEquals(Seq[Byte](-60, -4, 25, 0, -8, 65, 126, -44), buf.toSeq)
    OutputHelper(buf).writeDoubleLE(1.0)
    Assert.assertEquals(Seq[Byte](0, 0, 0, 0, 0, 0, -16, 63), buf.toSeq)
    OutputHelper(buf).writeDoubleLE(-2.123e24)
    Assert.assertEquals(Seq[Byte](-44, 126, 65, -8, 0, 25, -4, -60), buf.toSeq)


  @Test def testWriteStringUtf8(): Unit =
    val testString = "hello you guys"
    val length = testString.getBytes(InlineWriter.Utf8).length
    val buf = new Array[Byte](length)
    OutputHelper(buf).writeStringUtf8(testString)
    Assert.assertEquals(Seq[Byte](
      'h'.toByte,
      'e'.toByte,
      'l'.toByte,
      'l'.toByte,
      'o'.toByte,
      ' '.toByte,
      'y'.toByte,
      'o'.toByte,
      'u'.toByte,
      ' '.toByte,
      'g'.toByte,
      'u'.toByte,
      'y'.toByte,
      's'.toByte), buf.toSeq)
  // TODO - test an odd encoded character (something with a highpoint, like the eurosign.)

  @Test def testReadSimpleStringUtf8(): Unit =
    val testString = "hello you guys"
    val bytes = testString.getBytes(InlineWriter.Utf8)
    Assert.assertEquals(bytes.length, 14)
    Assert.assertEquals(testString, InputHelper(bytes).readStringUtf8(14))


  @Test def testReadTwoByteInStringUtf8(): Unit =
    val testString = "10¢"
    val bytes = testString.getBytes(InlineWriter.Utf8)
    Assert.assertEquals(bytes.length, 4)
    Assert.assertEquals(testString.size, 3)
    val result = InputHelper(bytes).readStringUtf8(4)
    Assert.assertEquals(testString(2).toInt, result(2).toInt)
    Assert.assertEquals(testString, result)

  @Test def testReadThreeByteInStringUtf8(): Unit =
    val testString = "10€"
    val bytes = testString.getBytes(InlineWriter.Utf8)
    Assert.assertEquals(5, bytes.length)
    Assert.assertEquals(3, testString.size)
    val result = InputHelper(bytes).readStringUtf8(5)
    Assert.assertEquals(testString(2).toInt, result(2).toInt)
    Assert.assertEquals(testString, result)

  @Test def testReadFourByteInStringUtf8(): Unit =
    val testString = "10𐍈"
    val bytes = testString.getBytes(InlineWriter.Utf8)
    Assert.assertEquals("We understand unicode", 66376, java.lang.Character.codePointAt(testString.toCharArray, 2))
    Assert.assertEquals(6, bytes.length)
    Assert.assertEquals(4, testString.size)
    val result = InputHelper(bytes).readStringUtf8(5)
    Assert.assertEquals("First two-part unicode character is right", testString(2).toInt, result(2).toInt)
    Assert.assertEquals("Secoond two-part unicode character is right", testString(3).toInt, result(3).toInt)
    Assert.assertEquals(testString, result)
    
