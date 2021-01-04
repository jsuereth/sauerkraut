package sauerkraut
package utils

// Helper methods for creating codecs.
object InlineWriter:
  final val Utf8 = java.nio.charset.Charset.forName("UTF-8")
  /** An enum representing where most-significant-bit is written in a byte sequence. */
  enum Endian:
    case Big, Little

  /** Writes a boolean value as a single byte, either 0 (false) or 1 (true). */
  inline def writeBoolean(value: Boolean, inline writeByte: (Byte) => Unit): Unit =
    writeByte(if value then 1 else 0)
  /** 
   * Writes a 16-bit integer value in specified Endian.
   * 
   * Note: this must be used to encode `Char` in java, unless using Varint format.
   */
  inline def writeFixed16(value: Short, inline writeByte: (Byte) => Unit, inline endian: Endian): Unit =
    inline endian match
      case Endian.Big =>
        writeByte((0xff & (value >> 8)).toByte)
        writeByte((0xff & value).toByte)
      case Endian.Little =>
        writeByte((0xff & value).toByte)
        writeByte((0xff & (value >> 8)).toByte)
  /** Writes a 32 bit integer in specified Endian.  That is, the highest byte written first. */
  inline def writeFixed32(value: Int, inline writeByte: (Byte) => Unit, inline endian: Endian): Unit =
    inline endian match
      case Endian.Big =>
        writeByte((0xff & (value >> 24)).toByte)
        writeByte((0xff & (value >> 16)).toByte)
        writeByte((0xff & (value >> 8)).toByte)
        writeByte((0xff & (value)).toByte)
      case Endian.Little =>
        writeByte((0xff & (value)).toByte)
        writeByte((0xff & (value >> 8)).toByte)
        writeByte((0xff & (value >> 16)).toByte)
        writeByte((0xff & (value >> 24)).toByte)
  /** Writes a 32 bit integer in specified Endian.  That is, the highest byte written first. */
  inline def writeFixed64(value: Long, inline writeByte: (Byte) => Unit, inline endian: Endian): Unit =
    inline endian match
      case Endian.Big =>
        writeByte((0xff & (value >> 56)).toByte)
        writeByte((0xff & (value >> 48)).toByte)
        writeByte((0xff & (value >> 40)).toByte)
        writeByte((0xff & (value >> 32)).toByte)
        writeByte((0xff & (value >> 24)).toByte)
        writeByte((0xff & (value >> 16)).toByte)
        writeByte((0xff & (value >> 8)).toByte)
        writeByte((0xff & (value)).toByte)
      case Endian.Little =>
        writeByte((0xff & (value)).toByte)
        writeByte((0xff & (value >> 8)).toByte)
        writeByte((0xff & (value >> 16)).toByte)
        writeByte((0xff & (value >> 24)).toByte)
        writeByte((0xff & (value >> 32)).toByte)
        writeByte((0xff & (value >> 40)).toByte)
        writeByte((0xff & (value >> 48)).toByte)
        writeByte((0xff & (value >> 56)).toByte)

  /** Writes a float to the stream using `Float.floatToIntBits` and fixed32 big endian encoding. */
  inline def writeFloat(value: Float, inline writeByte: (Byte) => Unit, inline endian: Endian): Unit = writeFixed32(java.lang.Float.floatToIntBits(value), writeByte, endian)
  /** Writes a Double to the stream using `Double.doubleToIntBits` and fixed32 big endian encoding. */
  inline def writeDouble(value: Double, inline writeByte: (Byte) => Unit, inline endian: Endian): Unit = writeFixed64(java.lang.Double.doubleToLongBits(value), writeByte, endian)

  /**
    * Writes the given integer value as a "varint".  In this format, the MSB represent whether or not more bytes should be read or not.
    * 
    * In this implementation, negative integers are FIRST cast to unsigned Long, then written, meaning they take ~10 bytes instead of 5.
    */
  inline def writeVarInt32(value: Int, inline writeByte: (Byte) => Unit): Unit = VarInt.writeUInt(value, writeByte)
  /**
    * Writes the given integer value as a "varint".  In this format, the MSB represent whether or not more bytes should be read or not.
    * 
    * All signed values are converted to unsigned before encoding.
    */
  inline def writeVarInt64(value: Long, inline writeByte: (Byte) => Unit): Unit = VarInt.writeULong(value, writeByte)
  /** Writes a string as UTF-8 encoded bytes. */
  inline def writeStringUtf8(value: String, inline writeBytes: (Array[Byte]) => Unit): Unit = writeBytes(value.getBytes(Utf8))
