package sauerkraut
package utils

/**
  * Helper for writing variable-sized integers
  */
object VarInt:
  /** Returns the size, in bytes, a varint will take when written. */
  def byteSize(value: Int): Int = varIntLength(value)+1
  /** Returns the size, in bytes, a varint will take when written. */
  def byteSize(value: Long): Int = varIntLength(value)+1

  /** Reads an Int32 from a byte buffer.  Note:  Negative values are encoded as UInt64, and interpreted. */
  def readInt(buffer: java.nio.ByteBuffer): Int = readUInt(buffer.get)
  /** Reads a varint (of max 64 bits) from the given byte buffer. */
  def readLong(buffer: java.nio.ByteBuffer): Long = readULong(buffer.get)

  /** Reads an Int32 from a byte buffer.  Note:  Negative values are encoded as UInt64, and interpreted. */
  def readInt(in: java.io.InputStream): Int = readUInt(() => in.read().toByte)
  /** Reads a varint (of max 64 bits) from the given byte buffer. */
  def readLong(in: java.io.InputStream): Long = readULong(() => in.read().toByte)

  /** Writes a varint to a byte buffer. */
  def write(value: Int, buffer: java.nio.ByteBuffer): Unit = writeUInt(value, buffer.put)
  /** Writes a varint to a byte buffer. */
  def write(value: Long, buffer: java.nio.ByteBuffer): Unit = writeULong(value, buffer.put)

  /** Writes a varint to a byte buffer. */
  def write(value: Int, out: java.io.OutputStream): Unit = writeUInt(value, out.write)
  /** Writes a varint to a byte buffer. */
  def write(value: Long, out: java.io.OutputStream): Unit = writeULong(value, out.write)

  // Helper methods / inline impls.

  inline def readULong(inline readNext: () => Byte): Long =
    var currentByte: Byte = readNext()
    if (currentByte & 0x80) == 0 then currentByte.toLong
    else
      var result: Long = currentByte & 0x7f
      var offset = 0
      while
        offset += 7
        currentByte = readNext()
        result |= (currentByte & 0x7F).toLong << offset
        (currentByte & 0x80) != 0 // && offset < 64 
      do ()
      result
  inline def readUInt(inline readNext: () => Byte): Int =
    var currentByte: Byte = readNext()
    if (currentByte & 0x80) == 0 then currentByte.toInt
    else
      var result: Int = currentByte & 0x7F
      var offset: Int = 0
      // Loop over bytes, pulling off MSB and shifting until we have all our bits.
      // TODO - unroll this for INT32 size?
      while
        offset += 7
        currentByte = readNext()
        result |= (currentByte & 0x7f) << offset
        (currentByte & 0x80 ) != 0 && offset < 32
      do ()
      // If we encode negative ints as 64-bit VarInts, keep reading those bytes.  This keeps us compatible
      // with proto buff.
      while
          (currentByte & 0x80) != 0 && offset < 64
      do
        offset += 7 
        currentByte = readNext()
      result


  // Note we use leading-zero optimisations as described here:
  // https://richardstartin.github.io/posts/dont-use-protobuf-for-telemetry
  private val VarIntLengths = (for (i <- 0 to 64) yield (63-i)/7).toArray
  private def varIntLength(value: Long): Int = VarIntLengths(java.lang.Long.numberOfLeadingZeros(value))
  inline def writeUInt(value: Int, inline writeByte: Byte => Unit): Unit =
    val length = varIntLength(value)
    var shiftedValue = value
    var i = 0
    while i < length do
      writeByte(((shiftedValue & 0x7F) | 0x80).toByte)
      shiftedValue >>>=7
      i += 1
    writeByte(shiftedValue.toByte)

  inline def writeULong(value: Long, inline writeByte: Byte => Unit): Unit =
    val length = varIntLength(value)
    var shiftedValue = value
    var i = 0
    while i < length do
      writeByte(((shiftedValue & 0x7F) | 0x80).toByte)
      shiftedValue >>>=7
      i += 1
    writeByte(shiftedValue.toByte)