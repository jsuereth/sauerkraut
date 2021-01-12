package sauerkraut.format.pb.streams


import sauerkraut.utils.InlineReader
import java.io.InputStream


/**
 * The interface from CodedInputStream we used to first implement protocol buffer reading.
 * 
 * We stick with this as close as possible for now, until we push our own impl given enough time / benchmarking
 * of alternative options.
 */
trait LimitableTagReadingStream:
  /**
   * Sets the current byte reading limit to `{current positon}` + `byteLimit`.
   * 
   * This limit only affects two portions of the input stream:
   * 1. `bytesLeftUntilLimit` will accurately return the number of bytes you can
   *    read before hitting the current limit.
   * 2. `readTag` will return 0 if `bytesLeftUntilLimit` is < 1, and will not
   *    read the underlying input until the current limit has been popped, or another
   *    limit has been pushed.
   */
  def pushLimit(byteLimit: Int): Int
  /**
   * Discards the current limit, returning to the previous limit.
   * 
   * @param oldLimit The last limit, as returned by `pushLimit`.
   */
  def popLimit(oldLimit: Int): Unit

  /** Performs an operation with a byte limit. */
  inline def withLimit[T](limit: Int, inline work: LimitableTagReadingStream => T): T =
    val last = pushLimit(limit)
    try work(this)
    finally popLimit(last)
  /**
   * @return True if there are no bytes left in the limit
   */
  def isAtEnd(): Boolean
  /** Reads the next field tag in a proto stream.
   * 
   * @return a Protocol Buffer tag value or `0` if we've reached the end of the
   *         stream or the current limit.
   */
  def readTag(): Int
  /**
   * Reads a single byte of input.
   */
  def readByte(): Byte
  /**
   * Reads an encoded boolean (one byte)
   */
  def readBoolean(): Boolean
  /**
   * Reads a VarInt of 32 bytes (positive inteegers) or 64 bytes (negative integers).
   */
  def readVarInt32(): Int
  /**
   * Reads a VarInt of 64 bytes maximum.
   */
  def readVarInt64(): Long
  /**
   * Reads a 32-bit floating point value.
   */
  def readFloat(): Float
  /**
   * Reads a 64-bit floating point value.
   */
  def readDouble(): Double
  /**
   * Reads a VarInt32 of length, then a UTF-8 encoded string in those bytes.
   */
  def readString(): String


/** Our version of a protocol buffer input stream for use by raw + pb formats. */
class ProtoInputStream(in: InputStream) extends LimitableTagReadingStream:
  private var position: Int = 0
  private var limit: Int = -1

  override final def pushLimit(byteLimit: Int): Int =
    val last = limit
    // If we've never used a limit, reset our position to avoid overflow.
    if last == -1 then position = 0
    limit = position + byteLimit
    last
  override final def popLimit(oldLimit: Int): Unit =
    limit = oldLimit
  /** 
   * Returns the number of bytes that can be read before reaching the current pushed limit.
   * If no limit is set, returns -1
   */
  final def bytesUntilLimit: Int = 
    if limit < 0 then Int.MaxValue else (limit - position)
  override final def isAtEnd(): Boolean = bytesUntilLimit < 1
  final def readNext(): Int = 
    position += 1
    in.read()

  // TODO - mechanism to read raw bytes?
  override final def readString(): String = 
    val length = readVarInt32()
    if (length > 0) then readString(length) else ""
  final def readString(lengthInBytes: Int): String = InlineReader.readStringUtf8(lengthInBytes, () => readNext())
  override final def readByte(): Byte = readNext().toByte
  override final def readBoolean(): Boolean = InlineReader.readBoolean(() => readNext())
  override final def readFloat(): Float = InlineReader.readFloat(() => readNext(), InlineReader.Endian.Little)
  override final def readDouble(): Double = InlineReader.readDouble(() => readNext(), InlineReader.Endian.Little)
  override final def readVarInt32(): Int = InlineReader.readVarInt32(() => readNext())
  override final def readVarInt64(): Long = InlineReader.readVarInt64(() => readNext())

  override def readTag(): Int =
    if (limit >= 0) && (bytesUntilLimit < 1) then 0
    else
      // TODO - Check for end-of-stream.
      val tag = readVarInt32()
      // Note: CodedOutputStream did this to us a lot.  Do we want to do this, or allow `Raw` format to be crazy?
      // if (WireFormat.extractField(tag) == 0) then throw InvalidTag(tag)

      // TODO - better EOF detection...
      if tag == -1 then 0 else tag