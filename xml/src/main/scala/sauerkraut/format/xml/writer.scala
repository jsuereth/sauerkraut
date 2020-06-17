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
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    out.write("<collection>")
    work(this)
    out.write("</collection>")
    this
  // TODO - maybe don't rely on toString on primitives...
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[_]): PickleWriter =
    out.write("<primitive>")
    tag match
      case PrimitiveTag.UnitTag => out.write("null")
      case PrimitiveTag.BooleanTag => out.write(picklee.asInstanceOf[Boolean].toString)
      case PrimitiveTag.CharTag | PrimitiveTag.StringTag =>
        // TODO - figure out whether or not to CDATA this.
        out.write(picklee.toString)
      case PrimitiveTag.ByteTag | PrimitiveTag.ShortTag | PrimitiveTag.IntTag | PrimitiveTag.LongTag =>
        // TODO - appropriate int handling
        out.write(picklee.toString)
      case PrimitiveTag.FloatTag | PrimitiveTag.DoubleTag =>
        // TODO - appropriate floating point handling
        out.write(picklee.toString)
    out.write("</primitive>")
    this
  override def putStructure(picklee: Any, tag: FastTypeTag[_])(work: PickleStructureWriter => Unit): PickleWriter =
    // TODO - tag...
    out.write("<structure>")
    work(this)
    out.write("</structure>")
    this

  override def flush(): Unit = out.flush()
  override def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
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
