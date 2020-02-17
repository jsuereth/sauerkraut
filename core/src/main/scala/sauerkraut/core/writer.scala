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


/** A Writer from some format of objects of the type T. */
trait Writer[T]
  def write(value: T, pickle: format.PickleWriter): Unit

object Writer
  import scala.compiletime.{erasedValue,summonFrom}
  import deriving._
  import internal.InlineHelper.summonLabel
  /** Derives writers of type T. */
  inline def derived[T](given m: Mirror.Of[T]): Writer[T] =
    new Writer[T] {
      def write(value: T, pickle: format.PickleWriter): Unit =
        inline m match
          case m: Mirror.ProductOf[T] =>
            pickle.putStructure(this, format.fastTypeTag[T]())(writer =>
            writeElems[m.MirroredElemTypes, m.MirroredElemLabels](writer, value, 0))
          case _ => compiletime.error("Cannot derive serialization for non-product classes")
    }
  /** Writes all the fields (in Elems) to the structure writer. */
  inline private def writeElems[Elems <: Tuple, Labels <: Tuple](
    pickle: format.PickleStructureWriter, value: Any, idx: Int): Unit =
      inline erasedValue[Elems] match
        case _: (elem *: elems1) =>
          pickle.putField(summonLabel[Labels](idx),
            fieldPickle =>
              writeInl[elem](productElement[elem](value, idx), fieldPickle))
          writeElems[elems1, Labels](pickle, value, idx+1)
        case _: Unit => ()
      
  /** Write a particular value to a pickle ready for it. Looks up given Writer. */
  inline private def writeInl[A](value: A, pickle: format.PickleWriter): Unit =
    summonFrom {
      case writer: Writer[A] => writer.write(value, pickle)
    }