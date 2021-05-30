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


class XmlPickleWriter(out: Writer) extends PickleWriter with PickleCollectionWriter with PickleStructureWriter:
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    out.write("<collection>")
    work(this)
    out.write("</collection>")
    this
  private inline def writePrimitive[A](f: => A): PickleWriter =
    out.write("<primitive>")
    f
    out.write("</primitive>")
    this
  // TODO - maybe don't rely on toString on primitives...
  override def putUnit(): PickleWriter = 
    writePrimitive(out.write("null"))
  override def putBoolean(value: Boolean): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putByte(value: Byte): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putChar(value: Char): PickleWriter = 
    writePrimitive(out.write(value.toString()))
  override def putShort(value: Short): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putInt(value: Int): PickleWriter = 
    writePrimitive(out.write(value.toString()))
  override def putLong(value: Long): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putFloat(value: Float): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putDouble(value: Double): PickleWriter =
    writePrimitive(out.write(value.toString()))
  override def putString(value: String): PickleWriter =
    // TODO - figure out whether or not to CDATA this.
    writePrimitive(out.write(value.toString()))
  override def putStructure(picklee: Any, tag: FastTypeTag[_])(work: PickleStructureWriter => Unit): PickleWriter =
    // TODO - tag...
    out.write("<structure>")
    work(this)
    out.write("</structure>")
    this

  override def putChoice(picklee: Any, tag: FastTypeTag[_], choice: String)(work: PickleWriter => Unit): PickleWriter =
    out.write("<choice type=\"")
    out.write(choice)
    out.write("\">")
    work(this)
    out.write("</choice>")
    this

  override def flush(): Unit = out.flush()
  override def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    out.write("<field name=\"")
    out.write(name)
    out.write("\">")
    pickler(this)
    out.write("</field>")
    this
  override def putElement(writer: PickleWriter => Unit): PickleCollectionWriter =
    out.write("<element>")
    writer(this)
    out.write("</element>")
    this
