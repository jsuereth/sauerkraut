package sauerkraut
package format
package nbt
package internal

import java.io.DataInputStream

class TagInputStream(in: DataInputStream)
  /** Reads the next NBT tag.
   * Note: This returns TagEnd on failure.
   */
  def readTag(): NbtTag =
    NbtTag.fromByte(in.readByte())

  /** Reads an encoded length, part of ListTag/CompoundTag. */
  def readLength(): Int = in.readInt()
  /** Reads a payload of the given pickle type. 
   * This must mirror TagOuputStream.writePayload.
   */
  def readPayload[T](tag: PrimitiveTag[T]): T =
    import PrimitiveTag._
    (tag match {
      case UnitTag => ()
      case ByteTag => in.readByte()
      case BooleanTag =>
        if (in.readByte() == 0) false else true
      case CharTag => in.readShort().toChar
      case ShortTag => in.readShort()
      case IntTag => in.readInt()
      case LongTag => in.readLong()
      case FloatTag => in.readFloat()
      case DoubleTag => in.readDouble()
      case StringTag => readName()
      // TODO Array[Long]
      // TODO Array[Byte]
      // TODO Array[Int]
    }).asInstanceOf[T]
  /** Reads an encoded string / name. */
  def readName(): String = in.readUTF()

  def close(): Unit = in.close()