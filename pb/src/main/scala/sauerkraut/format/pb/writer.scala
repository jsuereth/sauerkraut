package sauerkraut
package format
package pb

import com.google.protobuf.CodedOutputStream

/**
 * A PickleWriter that writes protocol-buffer-like pickles.   This will NOT
 * lookup appropriate field numbers per type, but instead number fields in order
 * it sees them as 1->N. This is ok for ephemeral serialization where there is no
 * class/definition skew, but not ok in most serialization applications.
 */
class RawProtocolBufferPickleWriter(out: CodedOutputStream) extends PickleWriter with PickleCollectionWriter
  def beginCollection(length: Int): PickleCollectionWriter =
    // When writing 'raw' collections, we just write a length, then each element.
    out.writeInt32NoTag(length)
    this
  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    pickler(this)
    this
  def endCollection(): Unit = ()

  // TODO - lookup known structure before using this.
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
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
  override def flush(): Unit = out.flush()

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


class ProtocolBufferFieldWriter(
    out: CodedOutputStream, 
    fieldNum: Int,
    optDescriptor: Option[TypeDescriptorMapping[?]] = None) 
    extends PickleWriter with PickleCollectionWriter
  // Writing a collection should simple write a field multiple times.
  def beginCollection(length: Int): PickleCollectionWriter = this
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    optDescriptor match
      case Some(d) =>
        // We need to write a header for this structure proto, which includes its size.
        // For now, we be lazy and write to temporary array, then do it all at once.
        // TODO - figure out if we can precompute and do this faster!
        val tmpByteOut = java.io.ByteArrayOutputStream()
        val tmpOut = CodedOutputStream.newInstance(tmpByteOut)
        val p = DescriptorBasedProtoStructureWriter(tmpOut, d, () => {
            tmpOut.flush()
            out.writeByteArray(fieldNum, tmpByteOut.toByteArray())
        })
        p
      // TODO - Configuration to allow raw
      case None => 
        ProtocolBufferPickleUnknownStructureWriter(out)

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

  override def flush(): Unit = out.flush()


/** This class can write out a proto structure given a TypeDescriptorMapping of field name to number. */
class DescriptorBasedProtoStructureWriter(
    out: CodedOutputStream,
    mapping: TypeDescriptorMapping[?],
    cleanup: () => Unit = () => ()) extends PickleStructureWriter
  override def endStructure(): Unit = cleanup()
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    val idx = mapping.fieldNumber(name)
    pickler(ProtocolBufferFieldWriter(out, idx, mapping.fieldDescriptor(name)))
    this

// TODO - migrate this to be based on TypeDescriptorRepository
class DescriptorBasedProtoWriter(
    out: CodedOutputStream,
    desc: TypeDescriptorMapping[?]
) extends PickleWriter
  def beginStructure(picklee: Any, tag: FastTypeTag[?]): PickleStructureWriter =
    DescriptorBasedProtoStructureWriter(out, desc)
  def putPrimitive(picklee: Any, tag: FastTypeTag[?]): Unit = ???
  def beginCollection(length: Int): PickleCollectionWriter = ???
  override def flush(): Unit = out.flush()
