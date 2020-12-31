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

class JsonPickleWriter(out: JsonOutputStream) extends PickleWriter with PickleChoiceWriter:
  override def writeUnit(): Unit = out.write("null")
  override def writeBoolean(value: Boolean): Unit = out.write(value.toString)
  override def writeByte(value: Byte): Unit = out.write(value.toString)
  override def writeChar(value: Char): Unit = 
    // TODO - is it more efficient to save as a number?
    out.write('"')
    out.write(value.toString)
    out.write('"')
  override def writeShort(value: Short): Unit = out.write(value.toString)
  override def writeInt(value: Int): Unit = out.write(value.toString)
  override def writeLong(value: Long): Unit = out.write(value.toString)
  override def writeFloat(value: Float): Unit = out.write(value.toString)
  override def writeDouble(value: Double): Unit = out.write(value.toString)
  override def writeString(value: String): Unit = 
    out.write('"')
    // TODO- escape the string
    out.write(value.toString)
    out.write('"')
  override def writeStructure[T: core.StructureWriter ](value: T): Unit =
    out.write('{')
    summon[core.StructureWriter[T]].writeStructure(value, JsonStructureWriter(out))
    out.write('}')
  override def writeCollection[T: core.CollectionWriter](value: T): Unit =
    out.write('[')
    summon[core.CollectionWriter[T]].writeCollection(value, JsonPickleCollectionWriter(out))
    out.write(']')
  override def writeChoice[T: core.ChoiceWriter](value: T): Unit =
    out.write('{')
    summon[core.ChoiceWriter[T]].writeChoice(value, this)
    out.write('}')
  override def flush(): Unit = out.flush()

  // As an optimisation, we write choices here, because there can only be one field.
  override def writeChoice[T: core.Writer](number: Int, name: String, value: T): Unit =
    // TODO - escape the name...
    out.write('"')
    out.write(name)
    out.write('"')
    out.write(':')
    summon[core.Writer[T]].write(value, JsonPickleWriter(out))

class JsonStructureWriter(out: JsonOutputStream) extends PickleStructureWriter:
  private var needsComma = false
  override def writeField[T: core.Writer](fieldNum: Int, fieldName: String, value: T): Unit =
    if (needsComma) out.write(',')
    // TODO - escape the name...
    out.write('"')
    out.write(fieldName)
    out.write('"')
    out.write(':')
    summon[core.Writer[T]].write(value, JsonPickleWriter(out))
    needsComma = true

class JsonPickleCollectionWriter(out: JsonOutputStream) extends PickleCollectionWriter:
  private var needsComma = false
  override def sizeHint(numElements: Int): Unit = ()
  override def writeElement[T: core.Writer](value: T): Unit =
    if (needsComma) out.write(',')
    summon[core.Writer[T]].write(value, JsonPickleWriter(out))
    needsComma = true