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
 * Currently all pickles can store three types of data:
 * 1. Primitive values (Int, String, etc.)  See [[FastTypeTag]] for definition of primitive.
 * 2. A structure of key-value pairs.
 * 3. A collection of other values.
 */
trait PickleWriter:
  /** Called to denote that an structure is about to be serialized.
    * @param picklee
    *                The structure to be serialized.
    * @param tag
    *                The tag to use when pickling this entry.   Tags must be serialized/restored, unless
    *                otherwise hinted that it can be elided.
    * @param work
    *                The function that will write the picklee to a pickle structure.
    *                Note: this may be called multiple times, e.g. when getting size estimates.
    */
  def putStructure(picklee: Any, tag: Struct[_])(work: PickleStructureWriter => Unit): PickleWriter
  /** Denotes an empty value. */
  def putUnit(): PickleWriter
  /** Writes a primtiive booelan value. */
  def putBoolean(value: Boolean): PickleWriter
  /** Writes a primtiive booelan value. */
  def putByte(value: Byte): PickleWriter
  /** Writes a primtiive char value. */
  def putChar(value: Char): PickleWriter
  /** Writes a primitive short value. */
  def putShort(value: Short): PickleWriter
  /** Writes a primtiive int value. */
  def putInt(value: Int): PickleWriter
  /** Writes a primtiive long value. */
  def putLong(value: Long): PickleWriter
  /** Writes a primtiive float value. */
  def putFloat(value: Float): PickleWriter
  /** Writes a primitive double value. */
  def putDouble(value: Double): PickleWriter
  /** Writes a "primitive" string value. */
  def putString(value: String): PickleWriter
  /**
   * Denotes that a collection of elements is about to be pickled.
   *
   * Note: This must be called after beginEntry()
   * @param length   The length of the collection being serialized.
   * @return  A pickler which can serialzie the collection.
   *          `endCollection()` must be called on this for correct behavior.
   */
  def putCollection(length: Int, tag: CollectionTag[_,_])(work: PickleCollectionWriter => Unit): PickleWriter
  /**
   * Denotes a 'choice' type that needs to be written.
   * 
   * Choices are the serialized equivalent of Sum types.  A choice is where one of several
   * different things could be serialized.    Formats are able to record the choice in any way
   * they desire.   See [[NonPrimitiveTag.Choice]] for information that can be used to distinguish
   * between options.
   */
  def putChoice(picklee: Any, tag: Choice[_], choice: String)(work: PickleWriter => Unit): PickleWriter
  /** Flush any pending writes down this writer. */
  def flush(): Unit

/** A mechanism to write a 'structure' to the pickle. 
 *  Structures are key-value pairs of 'fields'.
 */
trait PickleStructureWriter:
  /**
   * Serialize a "field" in a complex structure/object being pickled.
   * @param name  The name of the field to serialize.
   * @param pickler  A callback which will be passed an appropriate pickler.
   *                 You should ensure this function will perform a beginEntry()/endEntry() block.
   * @return A builder for remaining items in the current complex structure being pickled.
   */
  def putField(num: Int, name: String, pickler: PickleWriter => Unit): PickleStructureWriter

/** A writer of collection elements. */
trait PickleCollectionWriter:
   /**
   * Places the next element in the serialized collection.
   *
   * Note: This must be called after beginCollection().
   * @param pickler  A callback which is passed a pickler able to serialize the item in the collection.
   * @return  A pickler which can serialize the next element of the collection.
   */
  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter
