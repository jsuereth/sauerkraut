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

package sauerkraut.format

// TODO - redo this interface to match writing / or be friendly to avro/protocol buffers.

/** A reader of pickles.  This is the abstract interface we use to span different pickle format. */
trait PickleReader
  /** 
   * Start reading a pickled value.  
   *  This will return any serialized type tag key string.   This string can be used
   *  to reconstitute a FastTypeTag w/ a mirror, but is intended for use as fast string-matching.
   */
  def beginEntry(): String
  /** returns true if the reader is currently looking at a pickled primitive. */
  def atPrimitive: Boolean
  /** Reads one of the supported primitive types from the pickler. */
  def readPrimitive(): Any
  /** returns true if the reader is currently looking at a pickled object/structure. */
  def atObject: Boolean
  /** Returns a reader which can read a field of
   * a complex structure in the pickle.
   * @param name  The name of the field
   * @return  A reader which can read the structure's field.
   */
  def readField(name: String): PickleReader
  /** Denotes that we're done reading an entry in the pickle. */
  def endEntry(): Unit
  /** 
   * Denotes we'd like to read the current entry as a collection.
   * Note: Must be called after a beginEntry* call.
   */
  def beginCollection(): PickleReader
  /** Reads the length of a serialized collection.
    * Must be called directly after beginCollection and before readElement.
    * @return  The length of a serialized collection.
    */
  def readLength(): Int
  /** Returns a new Reader that can be used to read the next element in a collection.  */
  def readElement(): PickleReader
  /** Denote that we are done reading a collection. */
  def endCollection(): Unit
