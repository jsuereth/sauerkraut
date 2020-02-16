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

import scala.collection.mutable.Builder

/** A reader of pickles.  This is the abstract interface we use to span different pickle format. */
trait PickleReader
  // TODO - error/failures in reading?
  /** Reads a struct-like pickle.  This is where fields are stored by-name. */
  def readStructure[T](p: StructureReader => T): T
  /** Reads a primitive from the pickle.  See [[FastTypeTag]] for definition of primitives. */
  def readPrimitive[T](tag: FastTypeTag[T]): T
  // TODO - is this an ok interface for all collection-like things?
  def readCollection[E, To](
     builder: Builder[E, To],
     elementReader: PickleReader => E): To

/** A reader of structures within a pickle. */
trait StructureReader
  /** Reads a field with given name, using the lambda provided. */
  def readField[T](name: String, fieldReader: PickleReader => T): T

