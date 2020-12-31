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

import java.io.Writer


class XmlPickleWriter(out: Writer) extends PickleWriter with PickleCollectionWriter with PickleStructureWriter with PickleChoiceWriter:
  private inline def wrapPrimitive[T](f: => T): Unit =
    out.write("<primitive>")
    f
    out.write("</primitive>")
  override def writeUnit(): Unit = wrapPrimitive(())
  override def writeBoolean(value: Boolean): Unit = wrapPrimitive(out.write(value.toString))
  override def writeByte(value: Byte): Unit = wrapPrimitive(out.write(value.toString))
  override def writeChar(value: Char): Unit = wrapPrimitive(out.write(value.toString))
  override def writeShort(value: Short): Unit = wrapPrimitive(out.write(value.toString))
  override def writeInt(value: Int): Unit = wrapPrimitive(out.write(value.toString))
  override def writeLong(value: Long): Unit = wrapPrimitive(out.write(value.toString))
  override def writeFloat(value: Float): Unit = wrapPrimitive(out.write(value.toString))
  override def writeDouble(value: Double): Unit = wrapPrimitive(out.write(value.toString))
  override def writeString(value: String): Unit = wrapPrimitive(out.write(value))
  override def writeStructure[T: core.StructureWriter ](value: T): Unit =
    out.write("<structure>")
    summon[core.StructureWriter[T]].writeStructure(value, this)
    out.write("</structure>")
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    out.write("<collection>")
    summon[core.CollectionWriter[T]].writeCollection(value, this)
    out.write("</collection>")
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    out.write("<choice>")
    summon[core.ChoiceWriter[T]].writeChoice(value, this)
    out.write("</choice>")
  override def flush(): Unit = out.flush()
  override def writeField[T: core.Writer](fieldNum: Int, fieldName: String, value: T): Unit =
    out.write("<field name=\"")
    out.write(fieldName)
    out.write("\">")
    summon[core.Writer[T]].write(value, this)
    out.write("</field>")
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit =
    out.write("<element>")
    summon[core.Writer[T]].write(value, this)
    out.write("</element>")
  override def writeChoice[T: core.Writer](choiceNum: Int, choiceName: String, value: T): Unit =
    out.write("<field name=\"")
    out.write(choiceName)
    out.write("\">")
    summon[core.Writer[T]].write(value, this)
    out.write("</field>")
