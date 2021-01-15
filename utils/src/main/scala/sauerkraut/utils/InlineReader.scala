package sauerkraut
package utils

/** An exception thrown when UTF encoding is found to be in violation. */
class UtfEncodingException(msg: String) extends java.io.IOException(msg)

/**
 * Helper methods for creating cdoecs/formats.
 * 
 * This should avoid as much JVM-specific code as possible, over time.
 */
object InlineReader:

  /**
   * Reads a boolean value encoded as a single byte
   * 
   * @param readByte  Pulls the next byte from input.  It is expected that
   *                  this lambda will have already buffered the byte.
   */
  inline def readBoolean(inline readByte: () => Int): Boolean = readByte() != 0
  /**
   * Reads an integer of 16 bits.
   * 
   * @param readByte Pulls in the next byte from an input.  It is expected that
   *                 this lambda will have buffered 2 bytes of input prior to
   *                 calling this method.
   * @param endian  Whether MSB is at the beggining or end.
   */
  inline def readFixed16(inline readByte: () => Int, inline endian: Endian): Short =
    val b1 = readByte()
    val b2 = readByte()
    inline endian match
      case Endian.Big =>
        ((b1 << 8) + b2).toShort
      case Endian.Little =>
        (b1 + (b2 << 8)).toShort
  /**
   * Reads an integer of 32 bits.
   * 
   * @param readByte Pulls in the next byte from an input.  It is expected that
   *                 this lambda will have buffered 4 bytes of input prior to
   *                 calling this method.
   * @param endian  Whether MSB is at the beggining or end.
   */
  inline def readFixed32(inline readByte: () => Int, inline endian: Endian): Int =
    val b1 = readByte()
    val b2 = readByte()
    val b3 = readByte()
    val b4 = readByte()
    inline endian match
      case Endian.Big =>
        (b1 << 24) + (b2 << 16)  + (b3 << 8) + b4
      case Endian.Little =>
        b1 + (b2 << 8) + (b3 << 16) + (b4 << 24)
  /**
   * Reads an integer of 64 bits.
   * 
   * @param readByte Pulls in the next byte from an input.  It is expected that
   *                 this lambda will have buffered 8 bytes of input prior to
   *                 calling this method.
   * @param endian  Whether MSB is at the beggining or end.
   */
  inline def readFixed64(inline readByte: () => Int, inline endian: Endian): Long =
    val b1 = readByte()
    val b2 = readByte()
    val b3 = readByte()
    val b4 = readByte()
    val b5 = readByte()
    val b6 = readByte()
    val b7 = readByte()
    val b8 = readByte()
    inline endian match
      case Endian.Big =>
        (b1.toLong << 56) + 
        (b2.toLong << 48) + 
        (b3.toLong << 40) + 
        (b4.toLong << 32) + 
        (b5.toLong << 24) + 
        (b6 << 16)  + 
        (b7 << 8) + 
        b8
      case Endian.Little =>
        b1 + 
        (b2 << 8) + 
        (b3 << 16) + 
        (b4.toLong << 24) + 
        (b5.toLong << 32) + 
        (b6.toLong << 40) + 
        (b7.toLong << 48) + 
        (b8.toLong << 56)
  /**
   * Reads a floatingpoint value of 32bits.
   * 
   * The implementation reads an integer input and uses `Float.intBitsToFloat`
   * to convert back into floating point.
   * 
   * @param readByte Pulls in the next byte from an input.  It is expected that
   *                 this lambda will have buffered 4 bytes of input prior to
   *                 calling this method.
   * @param endian  Whether MSB is at the beggining or end.
   */
  inline def readFloat(inline readByte: () => Int, inline endian: Endian): Float =
    // TODO - expose an inline if we want to avoid JVM centric code...
    java.lang.Float.intBitsToFloat(readFixed32(readByte, endian))
  /**
    * Reads a floatingpoint value of 64bits.
    * 
    * The implementation reads an integer input and uses `Double.longBitsToDouble`
    * to convert back into floating point.
    * 
    * @param readByte Pulls in the next byte from an input.  It is expected that
    *                 this lambda will have buffered 8 bytes of input prior to
    *                 calling this method.
    * @param endian  Whether MSB is at the beggining or end.
    */
  inline def readDouble(inline readByte: () => Int, inline endian: Endian): Double =
    // TODO - expose an inline if we want to avoid JVM centric code...
    java.lang.Double.longBitsToDouble(readFixed64(readByte, endian))


  /**
   * Ensure a byte is a UTF-8 continuation byte (i.e. 10xx xxxx)
   * If it is, then return it without the two-byte header.
   */
  inline private def utf8ContinuationByte(byte: Int): Int =
    if (byte & 0x80) != 0x80 then
      throw UtfEncodingException(s"Expected utf continuation byte, found: ${(byte & 0xff).toBinaryString}")
    else (byte & 0x3f)

  /** 
   * Given a byte-length and a byte-reader, will decode UTF-8 strings.
   * 
   * This method throws `UtfEncodingException` when it encounters bad data, or if
   * the length cuts off a multi-byte character in the middle.
   * 
   * @param length The length of bytes to read.
   * @param readByte A lambda that will read the next available byte and advance the input stream.
   *                 It expected the byte reader will buffer the length ahead of time for efficiency.
   */
  inline def readStringUtf8(length: Int, inline readByte: () => Int): String = 
    var bytesRead = 0
    var charCount = 0
    // TODO - pre-allocate buffer somewhere, or take in a buffering strategy?
    val buf = new Array[Char](length*2)
    // First optimise for reading 1-byte characterss
    var currentByte: Int = 0
    // Note: Scala while-do is a bit mind-bending when you first appraoch it.
    // TL;DR the "expression" we use to evaluate whether to continue looping is
    // a side-effecting expression with early exit.
    // Here we're just trying to read all characters that fit in one byte.
    while 
      // force unsigned values
      currentByte = readByte() & 0xff
      bytesRead += 1
      val simpleCharacter = 
        if currentByte > 127 then
          // If we can't run the second loop, then break here.
          if bytesRead >= length then
             throw UtfEncodingException(s"Found continuation byte at end of string: ${(currentByte & 0xff).toBinaryString}")
          else false
        else
          buf(charCount) = currentByte.toChar
          charCount += 1
          true
      simpleCharacter && bytesRead < length
    do ()   
    // If bytesRead is not == length, then we ran into a unicode character that 
    // needs more than one byte to represent. NOW we deal with it.
    // `currentByte` will retain the first part of the higher-order unicode byte.
    // We need to check its header and determine how many more bytes we need to read, and continue
    // through the rest of the unicode string.
    while
      bytesRead < length
    do
      currentByte >> 4 match
        // handle two byte character (110xxxxx 10xxxxxx)
        case 12 | 13 => 
          // Handle two-byte character
          val secondByte = utf8ContinuationByte(readByte() & 0xff)
          buf(charCount) = (
              ((currentByte & 0x3f) << 6) + 
              secondByte
            ).toChar
          bytesRead += 1
          charCount += 1
        // handle three byte character (1110xxxx 10xxxxxx 10xxxxxx)
        case 14 =>
          // Handle three byte character
          val secondByte = utf8ContinuationByte(readByte() & 0xff)
          val thirdByte = utf8ContinuationByte(readByte() & 0xff)
          // combine bits in magical ways.
          buf(charCount) = (
            ((currentByte & 0x0f) << 12) + 
            (secondByte << 6) + 
            thirdByte
          ).toChar
          bytesRead += 2
          charCount += 1
        // handle four byte character (11110xxx 10xxxxxx 10xxxxxx 10xxxxxx)
        case 15 =>
          val secondByte = utf8ContinuationByte(readByte() & 0xff)
          val thirdByte = utf8ContinuationByte(readByte() & 0xff)
          val fourthByte = utf8ContinuationByte(readByte() & 0xff)
          // Grab the integer codepoint.
          val codePoint = (
            ((currentByte & 0x07) << 18) +
            (secondByte  << 12) +
            (thirdByte << 6) +
            fourthByte
          )
          // Now grab the JVM reprsentation, yay jvm.
          // TODO - Do we need to check if codepoint size is > 1?
          // TODO - Expose another inline or two to avoid JVM-centric code.
          buf(charCount) = java.lang.Character.highSurrogate(codePoint)
          buf(charCount+1) = java.lang.Character.lowSurrogate(codePoint)
          bytesRead += 3
          charCount += 2
        // handle one-byte character
        case x if x < 7 =>
          buf(charCount) = currentByte.toChar
          charCount += 1
        case _ => 
          throw UtfEncodingException(s"Encountered invalid byte: ${currentByte.toBinaryString}.  Expected valid UTF header.")
      // Read next byte and end of do, if we have remaining bytes.
      if bytesRead < length then
        currentByte = readByte()
        bytesRead += 1
    // Finally create a string from our character buffers.
    new String(buf, 0, charCount)
  
  /**
    * Reads a variable-length encoded integer of (maximum) 64bits.
    * 
    * @param readByte a function that will read the next byte of data.
    *                 It is expected that any buffering of data is done
    *                 outside thiis method.  Additonally, failure to pull
    *                 a byte is expected to throw an exception.
    */
  inline def readVarInt64(inline readByte: () => Int): Long =
    VarInt.readULong(() => readByte().toByte)
   /**
    * Reads a variable-length encoded integer of (maximum) 64bits.
    * 
    * Note: The integer value is expected to be within the 32-bit range,
    * but negative integers will be encoded as unsigned 64-bit integers.
    * 
    * @param readByte a function that will read the next byte of data.
    *                 It is expected that any buffering of data is done
    *                 outside thiis method.  Additonally, failure to pull
    *                 a byte is expected to throw an exception.
    */
  inline def readVarInt32(inline readByte: () => Int): Int =
    VarInt.readUInt(() => readByte().toByte)