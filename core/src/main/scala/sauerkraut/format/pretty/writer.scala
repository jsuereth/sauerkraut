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
  override def putUnit(): PickleWriter = this
  override def putBoolean(value: Boolean): PickleWriter =
    out.write(value.toString())
    this
  override def putByte(value: Byte): PickleWriter =
    out.write(value.toString())
    this
  override def putChar(value: Char): PickleWriter = 
    out.write(value.toString())
    this
  override def putShort(value: Short): PickleWriter =
    out.write(value.toString())
    this
  override def putInt(value: Int): PickleWriter = 
    out.write(value.toString())
    this
  override def putLong(value: Long): PickleWriter =
    out.write(value.toString())
    this
  override def putFloat(value: Float): PickleWriter =
    out.write(value.toString())
    this
  override def putDouble(value: Double): PickleWriter =
    out.write(value.toString())
    this
  override def putString(value: String): PickleWriter =
    out.write(value)
    this
  override def putCollection(length: Int, tag: CollectionTag[?,?])(work: PickleCollectionWriter => Unit): PickleWriter =
    if (length == 0) then
      out.write("[]")
    else
        // TODO - actually size the collection and fit it into a line/determine how to break it up...
        out.write('[')
        work(PrettyPrintCollectionWriter(out, indent))
        out.write(']')
    this
  override def putStructure(picklee: Any, tag: Struct[?])(work: PickleStructureWriter => Unit): PickleWriter =
    out.write(tag.toString)
    out.write(" {")
    work(PrettyPrintStructureWriter(out, indent+1))
    out.write("}")
    this

  override def putChoice(picklee: Any, tag: Choice[?], choice: String)(work: PickleWriter => Unit): PickleWriter =
    out.write(tag.toString)
    out.write(" {")
    out.write("\n")
    out.write(indentSpace)
    out.write("  ")
    out.write(choice)
    out.write(": ")
    work(PrettyPrintPickleWriter(out, indent+1))
    out.write("\n")
    out.write(indentSpace)
    out.write("}")
    this

  override def flush(): Unit = out.flush()

class PrettyPrintStructureWriter(out: Writer, indent: Int) extends PickleStructureWriter:
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
  override def putField(num: Int, name: String, writer: PickleWriter => Unit): PickleStructureWriter =
    writePrefix()
    out.write(name)
    out.write(": ")
    writer(PrettyPrintPickleWriter(out, indent))
    writePostFix()
    this
class PrettyPrintCollectionWriter(out: Writer, indent: Int) extends PickleCollectionWriter:
  var first = true
  // TODO - Determine when to roll over to new line
  override def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter =
    if first then
      first = false
    else
      out.write(", ")
    pickler(PrettyPrintPickleWriter(out, indent))
    this