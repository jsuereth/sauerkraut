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
package json

import java.io.StringWriter

type JsonOutputStream = StringWriter

class JsonPickleWriter(out: JsonOutputStream) extends PickleWriter
  override def putCollection(length: Int)(work: PickleCollectionWriter => Unit): PickleWriter =
    out.write('[')
    work(JsonPickleCollectionWriter(out))
    out.write(']')
    this
  // TODO - maybe don't rely on toString on primitives...
  override def putPrimitive(picklee: Any, tag: PrimitiveTag[_]): PickleWriter =
    tag match
      case PrimitiveTag.UnitTag => out.write("null")
      case PrimitiveTag.BooleanTag => out.write(picklee.asInstanceOf[Boolean].toString)
      case PrimitiveTag.CharTag | PrimitiveTag.StringTag => 
        out.write('"')
        out.write(picklee.toString)
        out.write('"')
      case PrimitiveTag.ShortTag | PrimitiveTag.IntTag | PrimitiveTag.LongTag =>
        // TODO - appropriate int handling
        out.write(picklee.toString)
      case PrimitiveTag.FloatTag | PrimitiveTag.DoubleTag =>
        // TODO - appropriate floating point handling
        out.write(picklee.toString)
    this

  override def putStructure(picklee: Any, tag: FastTypeTag[_])(work: PickleStructureWriter => Unit): PickleWriter =
    out.write('{')
    work(JsonStructureWriter(out))
    out.write('}')
    this

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

class JsonPickleCollectionWriter(out: JsonOutputStream) extends PickleCollectionWriter
  private var needsComma = false
  def putElement(writer: PickleWriter => Unit): PickleCollectionWriter =
    if (needsComma) out.write(',')
    writer(JsonPickleWriter(out))
    needsComma = true
    this