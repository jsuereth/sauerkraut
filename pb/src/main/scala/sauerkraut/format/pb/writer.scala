package sauerkraut
package format
package pb

import com.google.protobuf.CodedOutputStream


class ProtocolBufferPickleWriter(out: CodedOutputStream) extends PickleWriter {
  def beginCollection(length: Int): PickleCollectionWriter = ???
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter = ???
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
}