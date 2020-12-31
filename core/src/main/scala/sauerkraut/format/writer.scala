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


/**
 * A writer of pickled content.
 * 
 * Currently all pickles can store four types of data:
 * 1. Primitive values (`Int`, `String`, etc.)
 * 2. A structure of key-value pairs.
 * 3. A collection of other values.
 * 4. A "choice" between options.
 */
trait PickleWriter:
  /** Denotes an empty value. */
  def writeUnit(): Unit
  /** Writes a primtiive booelan value. */
  def writeBoolean(value: Boolean): Unit
  /** Writes a primtiive booelan value. */
  def writeByte(value: Byte): Unit
  /** Writes a primtiive char value. */
  def writeChar(value: Char): Unit
  /** Writes a primitive short value. */
  def writeShort(value: Short): Unit
  /** Writes a primtiive int value. */
  def writeInt(value: Int): Unit
  /** Writes a primtiive long value. */
  def writeLong(value: Long): Unit
  /** Writes a primtiive float value. */
  def writeFloat(value: Float): Unit
  /** Writes a primitive double value. */
  def writeDouble(value: Double): Unit
  /** Writes a "primtiive" string value. */
  def writeString(value: String): Unit
  /** Writes a structured value into the pickle. */
  def writeStructure[T: core.StructureWriter ](value: T): Unit
  /** Writes a Collection of values into the pickle. */
  def writeCollection[T: core.CollectionWriter](value: T): Unit
  /** Writes a choice between values into the pickle. */
  def writeChoice[T: core.ChoiceWriter](value: T): Unit
  /** Flush any pending writes down this writer. */
  def flush(): Unit

/** A mechanism to write a 'structure' to the pickle. 
 *  Structures are key-value pairs of 'fields'.
 */
trait PickleStructureWriter:
  /** 
   * Write a field to the structure which is an object, itself. 
   *  - Note: The object may be a Choice, Collection or Structure
   */
  def writeField[T: core.Writer](fieldNum: Int, fieldName: String, value: T): Unit

/** A writer of collection elements. */
trait PickleCollectionWriter:
  def sizeHint(numElements: Int): Unit
  def writeElement[T: core.Writer](value: T): Unit

/** A writer of a choice among options. */
trait PickleChoiceWriter:
  /** 
   * Write a choice value of an object.
   *  - Note: The object may itself be a Choice, Collection or Structure
   */
  def writeChoice[T: core.Writer](choiceNum: Int, choiceName: String, value: T): Unit