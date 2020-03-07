/*
 * Copyright 2019 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sauerkraut
package format
package xml

import core.{
  Builder,
  StructureBuilder,
  CollectionBuilder,
  PrimitiveBuilder,
  ChoiceBuilder
}

import javax.xml.parsers.{
  SAXParser,
  SAXParserFactory
}
import org.xml.sax.{
  Attributes,
  SAXException,
  ContentHandler
}
import org.xml.sax.helpers.DefaultHandler
import java.util.Stack
import java.io.InputStream

def inputStreamSaxReader(in: InputStream)(handler: DefaultHandler): Unit =
  val spf = SAXParserFactory.newInstance
  spf.setNamespaceAware(true)
  val saxParser = spf.newSAXParser()
  saxParser.parse(in, handler)

class XmlReader(reader: DefaultHandler => Unit) extends PickleReader
  override def push[T](builder: Builder[T]): Builder[T] =
    reader(XmlContentHandler(builder))
    builder

class XmlContentHandler(b: Builder[?]) extends DefaultHandler
  private var currentBuilder: Builder[?] = b
  private val stack: Stack[Builder[?]] = Stack()

  private var hadValue = false

  override def characters(ch: Array[Char], start: Int, length: Int): Unit =
    // Note: We only see primitives in string fields.
    val value = new String(ch, start, length)
    currentBuilder match
      case pb: PrimitiveBuilder[_] => putPrimitive(pb, value)
      case _ => throw WriteException(s"Unable to push value: $value into $currentBuilder", null)

  private def putPrimitive[T](p: PrimitiveBuilder[T], value: String): Unit =
    hadValue = true
    p.tag match
      case PrimitiveTag.UnitTag => ()
      case PrimitiveTag.BooleanTag => p.putPrimitive(value.toBoolean)
      case PrimitiveTag.ByteTag => p.putPrimitive(value.toByte)
      case PrimitiveTag.CharTag => p.putPrimitive(value(0))
      case PrimitiveTag.ShortTag => p.putPrimitive(value.toShort)
      case PrimitiveTag.IntTag => p.putPrimitive(value.toInt)
      case PrimitiveTag.LongTag => p.putPrimitive(value.toLong)
      case PrimitiveTag.FloatTag => p.putPrimitive(value.toFloat)
      case PrimitiveTag.DoubleTag => p.putPrimitive(value.toDouble)
      case PrimitiveTag.StringTag => p.putPrimitive(value)

  override def startElement(uri: String, localName: String, qName: String, atts: Attributes): Unit =
    localName match
      case "element" =>
        currentBuilder match
          case cb: CollectionBuilder[_,_] => 
            stack.push(currentBuilder)
            currentBuilder = cb.putElement()
          case _ => throw WriteException(s"Unable to push element into $currentBuilder", null)
      case "field" =>
        currentBuilder match
          case sb: StructureBuilder[_] =>
            stack.push(currentBuilder)
            currentBuilder = sb.putField(atts.getValue("name"))
          case cb: ChoiceBuilder[_] =>
            stack.push(currentBuilder)
            currentBuilder = cb.putChoice(atts.getValue("name"))
          case _ => throw WriteException(s"Unable to push field ${atts.getValue("name")} into $currentBuilder", null)
      case "collection" => ()
      case "structure" => ()
      case "primitive" =>
        hadValue = false
  override def endElement(uri: String, localName: String, qName: String): Unit =
    localName match
      case "primitive" =>
        if (!hadValue) characters(Array(), 0, 0)
      case "field" | "element" =>
        currentBuilder = stack.pop()
      case _ =>