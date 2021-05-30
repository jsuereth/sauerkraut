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

import java.io.Writer

type JsonOutputStream = Writer

class JsonPickleWriter(out: JsonOutputStream) extends PickleWriter:
  override def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter =
    out.write('[')
    work(JsonPickleCollectionWriter(out))
    out.write(']')
    this
  // TODO - maybe don't rely on toString on primitives...
  override def putUnit(): PickleWriter =
    out.write("null")
    this
  override def putBoolean(value: Boolean): PickleWriter =
    out.write(value.toString())
    this
  override def putByte(value: Byte): PickleWriter =
    out.write(value.toString())
    this
  override def putChar(value: Char): PickleWriter = 
    out.write('"')
    out.write(value.toString())
    out.write('"')
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
    out.write('"')
    out.write(value)
    out.write('"')
    this
  override def putStructure(picklee: Any, tag: Struct[_])(work: PickleStructureWriter => Unit): PickleWriter =
    out.write('{')
    work(JsonStructureWriter(out))
    out.write('}')
    this
  override def putChoice(picklee: Any, tag: Choice[_], choice: String)(work: PickleWriter => Unit): PickleWriter =
    // For now, we encode choice as its own structure with a guiding choice value.
    // Ideally we could encode the chocie tag in the underlying structure (if it is a struct).
    out.write('{')
    out.write('"')
    out.write(choice)
    out.write('"')
    out.write(':')
    work(this)
    out.write('}')
    this

  override def flush(): Unit = out.flush()


class JsonStructureWriter(out: JsonOutputStream) extends PickleStructureWriter:
  private var needsComma = false
  def putField(number: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter =
    if (needsComma) out.write(',')
    // TODO - escape the name...
    out.write('"')
    out.write(name)
    out.write('"')
    out.write(':')
    pickler(JsonPickleWriter(out))
    needsComma = true
    this

class JsonPickleCollectionWriter(out: JsonOutputStream) extends PickleCollectionWriter:
  private var needsComma = false
  def putElement(writer: PickleWriter => Unit): PickleCollectionWriter =
    if (needsComma) out.write(',')
    writer(JsonPickleWriter(out))
    needsComma = true
    this