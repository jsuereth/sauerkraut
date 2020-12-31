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
package pretty

import java.io.Writer

/** Pretty prints to a character stream. */
class PrettyPrintPickleWriter(out: Writer, indent: Int = 0) extends PickleWriter:
  private def indentSpace: String = (0 until indent).map(_ => ' ').mkString("")
  override def writeUnit(): Unit = ()
  override def writeBoolean(value: Boolean): Unit = out.write(value.toString())
  override def writeByte(value: Byte): Unit = out.write(value.toString())
  override def writeChar(value: Char): Unit = out.write(value.toString())
  override def writeShort(value: Short): Unit = out.write(value.toString())
  override def writeInt(value: Int): Unit = out.write(value.toString())
  override def writeLong(value: Long): Unit = out.write(value.toString())
  override def writeFloat(value: Float): Unit = out.write(value.toString())
  override def writeDouble(value: Double): Unit = out.write(value.toString())
  override def writeString(value: String): Unit = out.write(value)
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    out.write('[')
    summon[core.CollectionWriter[T]].writeCollection(value, PrettyPrintCollectionWriter(out,indent))
    out.write(']')
  override def writeStructure[T: core.StructureWriter](value: T): Unit =
    val writer = summon[core.StructureWriter[T]]
    out.write(writer.tag.toString)
    out.write(" {")
    writer.writeStructure(value, PrettyPrintStructureWriter(out, indent+1))
    out.write("}")
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    val writer = summon[core.ChoiceWriter[T]]
    out.write(writer.tag.toString)
    out.write(" {")
    writer.writeChoice(value, PrettyPrintStructureWriter(out, indent+1))
    out.write("}")
  override def flush(): Unit = out.flush()

class PrettyPrintStructureWriter(out: Writer, indent: Int) extends PickleStructureWriter with PickleChoiceWriter:
  private var first = true
  private def indentSpace(n: Int = indent): Unit = 
      (0 until n).foreach(_ => out.write("  "))
  private def writePrefix(): Unit =
      if (first) 
        out.write("\n")
        indentSpace()
        first = false
      else out.write("  ")
  private def writePostFix(): Unit =
    out.write("\n")
    indentSpace(indent-1)
  override def writeField[T: core.Writer](num: Int, name: String, value: T): Unit =
    val writer = summon[core.Writer[T]]
    writePrefix()
    out.write(name)
    out.write(": ")
    writer.write(value, PrettyPrintPickleWriter(out, indent))
    writePostFix()
  override def writeChoice[T: core.Writer](num: Int, name: String, value: T): Unit =
    val writer = summon[core.Writer[T]]
    writePrefix()
    out.write(name)
    out.write(": ")
    writer.write(value, PrettyPrintPickleWriter(out, indent))
    writePostFix()

class PrettyPrintCollectionWriter(out: Writer, indent: Int) extends PickleCollectionWriter:
  var first = true
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit =
    if first then
      first = false
    else
      out.write(", ")
    summon[core.Writer[T]].write(value, PrettyPrintPickleWriter(out, indent))