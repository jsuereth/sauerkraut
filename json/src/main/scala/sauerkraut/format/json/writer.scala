package sauerkraut
package format
package json

import java.io.StringWriter

type JsonOutputStream = StringWriter

class JsonPickleWriter(out: JsonOutputStream) extends PickleWriter
  override def beginCollection(length: Int): PickleCollectionWriter =
    out.write('[')
    JsonPickleCollectionWriter(out)
  // TODO - maybe don't rely on toString on primitives...
  override def putPrimitive(picklee: Any, tag: FastTypeTag[_]): Unit =
    tag match
      case FastTypeTag.UnitTag => out.write("null")
      case FastTypeTag.BooleanTag => out.write(picklee.asInstanceOf[Boolean].toString)
      case FastTypeTag.CharTag | FastTypeTag.StringTag => 
        out.write('"')
        out.write(picklee.toString)
        out.write('"')
      case FastTypeTag.ShortTag | FastTypeTag.IntTag | FastTypeTag.LongTag =>
        // TODO - appropriate int handling
        out.write(picklee.toString)
      case FastTypeTag.FloatTag | FastTypeTag.DoubleTag =>
        // TODO - appropriate floating point handling
        out.write(picklee.toString)
      case _ => ???

  override def beginStructure(picklee: Any, tag: FastTypeTag[_]): PickleStructureWriter =
    out.write('{')
    JsonStructureWriter(out)

  override def flush(): Unit = ()


class JsonStructureWriter(out: JsonOutputStream) extends PickleStructureWriter
  private var needsComma = false
  def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    if (needsComma) out.write(',')
    // TODO - escape the name...
    out.write('"')
    out.write(name)
    out.write('"')
    out.write(':')
    pickler(JsonPickleWriter(out))
    needsComma = true
    this
  def endStructure(): Unit = out.write('}')

class JsonPickleCollectionWriter(out: JsonOutputStream) extends PickleCollectionWriter
  private var needsComma = false
  def putElement(writer: PickleWriter => Unit): PickleCollectionWriter =
    if (needsComma) out.write(',')
    writer(JsonPickleWriter(out))
    needsComma = true
    this
  def endCollection(): Unit = out.write(']')