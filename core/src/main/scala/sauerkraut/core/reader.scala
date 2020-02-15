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
package core


/** A reader from some format of objects of the type T. */
trait Reader[T]
  // TODO - do we need a `tag` to disambiguate the thing we're reading?
  def read(pickle: format.PickleReader): T

object Reader
  import deriving._
  import scala.compiletime.{erasedValue,constValue,summonFrom}
  import internal.InlineHelper.summonLabel
  /** Derives a Reader for Product/Sum types using readers available on given scope. */
  inline def derived[T](given m: Mirror.Of[T]): Reader[T] =
    new Reader[T] {
      def read(pickle: format.PickleReader): T =
        inline m match
          case m: Mirror.ProductOf[T] =>
            pickle.readStructure { structReader =>
              m.fromProduct(
                readFields[m.MirroredElemTypes, m.MirroredElemLabels](
                  structReader, 0).asInstanceOf).asInstanceOf[T]
            }
          case _ => compiletime.error("Cannot derive serialization for non-product structures.")
    }

  inline private def readFields[Fields <: Tuple, Labels <: Tuple](pickle: format.StructureReader, idx: Int): Fields =
    inline erasedValue[Fields] match {
      case _: (elem *: elems) => 
        (readField[elem](pickle, summonLabel[Labels](idx)) *: readFields[elems, Labels](pickle, idx+1)).asInstanceOf[Fields]
      case _: Unit=> ().asInstanceOf[Fields]
    }

  inline private def readField[T](
    pickle: format.StructureReader,
    fieldName: String
  ): T =
    summonFrom {
      case reader: Reader[T] => pickle.readField(fieldName, reader.read)
    }