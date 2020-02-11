package sauerkraut
package format
package pb

import com.google.protobuf.CodedOutputStream


class RawProtocolBufferPickleWriter(out: CodedOutputStream) extends PickleWriter
  def beginCollection(length: Int): PickleCollectionWriter =
    // TODO - Support raw collections.
    ???
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    // TODO - lookup known structure before using this.
    ProtocolBufferPickleUnknownStructureWriter(out)
  def putPrimitive(picklee: Any, tag: FastTypeTag[?]): Unit =
    tag match
      case FastTypeTag.UnitTag => ()
      case FastTypeTag.BooleanTag => out.writeBoolNoTag(picklee.asInstanceOf[Boolean])
      case FastTypeTag.CharTag => out.writeInt32NoTag(picklee.asInstanceOf[Char].toInt)
      case FastTypeTag.ShortTag => out.writeInt32NoTag(picklee.asInstanceOf[Short].toInt)
      case FastTypeTag.IntTag => out.writeInt32NoTag(picklee.asInstanceOf[Int])
      case FastTypeTag.LongTag => out.writeInt64NoTag(picklee.asInstanceOf[Long])
      case FastTypeTag.FloatTag => out.writeFloatNoTag(picklee.asInstanceOf[Float])
      case FastTypeTag.DoubleTag => out.writeDoubleNoTag(picklee.asInstanceOf[Double])
      case FastTypeTag.StringTag => out.writeStringNoTag(picklee.asInstanceOf[String])
      case FastTypeTag.Erased() => ???

/** 
 * An unknown protocol buffer structure writer.  It simply gives all new fields
 * a new index, starting with 1 and moving up.
 */
class ProtocolBufferPickleUnknownStructureWriter(out: CodedOutputStream) extends PickleStructureWriter
  private var currentFieldIndex = 0
  def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    currentFieldIndex += 1
    pickler(ProtocolBufferFieldWriter(out, currentFieldIndex))
    this
  def endStructure(): Unit = ()


class ProtocolBufferFieldWriter(out: CodedOutputStream, fieldNum: Int) 
    extends PickleWriter with PickleCollectionWriter
  // Writing a collection should simple write a field multiple times.
  def beginCollection(length: Int): PickleCollectionWriter = this
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    // TODO - We need to grab size of the output to write this out, so likely need a nested writer to capture size...
    ???
  def putPrimitive(picklee: Any, tag: FastTypeTag[?]): Unit =
    tag match
      case FastTypeTag.UnitTag => ()
      case FastTypeTag.BooleanTag => out.writeBool(fieldNum, picklee.asInstanceOf[Boolean])
      case FastTypeTag.CharTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Char].toInt)
      case FastTypeTag.ShortTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Short].toInt)
      case FastTypeTag.IntTag => out.writeInt32(fieldNum, picklee.asInstanceOf[Int])
      case FastTypeTag.LongTag => out.writeInt64(fieldNum, picklee.asInstanceOf[Long])
      case FastTypeTag.FloatTag => out.writeFloat(fieldNum, picklee.asInstanceOf[Float])
      case FastTypeTag.DoubleTag => out.writeDouble(fieldNum, picklee.asInstanceOf[Double])
      case FastTypeTag.StringTag => out.writeString(fieldNum, picklee.asInstanceOf[String])
      case FastTypeTag.Erased() => ???

  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this

  def endCollection(): Unit = ()