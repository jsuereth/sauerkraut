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

import scala.collection.mutable.Builder
/**
 * A reader of pickles that pushes into a builder of type T.
 */
trait PicklePushReader
  /** Pushes the contents of the pickle into the builder. */
  def push[T](builder: core.Builder[T]): core.Builder[T]

/** 
 * A reader of pickles.
 *   
 * This is the abstract interface we use to span different pickle format.
 * 
 * Pickles are currently one of three things:
 * 1. A primitive value (Int, String, etc.)
 * 2. A structure of name-value pairs.
 * 3. A collection of values of the same type.
 * 
 * This uses a `pull` model for reading the pickle.  The [[Reader]]
 * is expected to draw information out of a pickle using this interface.
 * 
 * TODO - We, likely, should push into [[Reader]] to improve performance.
 */
trait PickleReader
  // TODO - error/failures in reading?
  /** Reads a struct-like pickle.  This is where fields are stored by-name. */
  def readStructure[T](p: StructureReader => T): T
  /** Reads a primitive from the pickle.  See [[FastTypeTag]] for definition of primitives. */
  def readPrimitive[T](tag: PrimitiveTag[T]): T
  // TODO - is this an ok interface for all collection-like things?
  def readCollection[E, To](
     builder: Builder[E, To],
     elementReader: PickleReader => E): To

/** 
 * A reader of structures within a pickle.
 * 
 * Structures are data consisting of name-value pairs.
 * 
 * Note: We want a push/pull mechanic here for some formats.
 * 
 * 1. We want [[core.Reader]] to be able to *push* field names
 *    that are expected to the format.
 * 2. We want [[Format]] to be able to push currently iterating fields
 *    *back* to the [[core.Reader]], as we cannot guarantee reading order
 *    will be the same as writing order AND there may be unknown fields
 *    when reading.
 */
trait StructureReader 

  /** Reads a field with given name, using the lambda provided. */
  def readField[T](name: String, fieldReader: PickleReader => T): T

