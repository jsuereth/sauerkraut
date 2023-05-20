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
trait Writer[T]:
  def write(value: T, pickle: format.PickleWriter): Unit
  /** Type tag for what this can write. */
  def tag: format.FastTypeTag[T]

object Writer:
  import scala.compiletime.{constValue,erasedValue,summonFrom}
  import deriving._
  import internal.InlineHelper.summonLabel
  import internal.MacroHelper.fieldNum
  /** Derives writers of type T. */
  inline def derived[T](using m: Mirror.Of[T]): Writer[T] =
    new Writer[T] {
      override val tag: format.FastTypeTag[T] = format.fastTypeTag[T]()
      override def write(value: T, pickle: format.PickleWriter): Unit =
        inline m match
          case m: Mirror.ProductOf[T] =>
            writeStruct[T, m.MirroredElemTypes, m.MirroredElemLabels](
              value,
              pickle,
              tag)
          case m: Mirror.SumOf[T] =>
            // TODO - We may need to synthesize writers for each option.
            writeOption[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]](
              value, 
              pickle,
              tag)
          case _ => compiletime.error("Cannot derive serialization for non-product classes")
    }
  /** Writes all the fields (in Elems) to the structure writer. */
  inline private def writeFields[ProductType, FieldTypes <: Tuple, Labels <: Tuple](
    pickle: format.PickleStructureWriter, value: Any, idx: Int): Unit =
      inline erasedValue[FieldTypes] match
        case _: (fieldType *: fieldTypes) =>
          inline erasedValue[Labels] match
            case _: (fieldLabel *: fieldLabels) =>
              writeField[ProductType, fieldType, fieldLabel](pickle, value, idx)
              writeFields[ProductType, fieldTypes, fieldLabels](pickle, value, idx+1)
            case _: EmptyTuple => compiletime.error("LOGIC ERROR: Ran out of field labels for field types")
        case _: EmptyTuple => ()

  inline private def writeField[ProductType, FieldType, Label](pickle: format.PickleStructureWriter, value: Any, idx: Int): Unit =
    inline constValue[Label] match
      case name: String => 
        pickle.putField(fieldNum[ProductType](name), name, fieldPickle =>
          writeInl[FieldType](value.asInstanceOf[Product].productElement(idx).asInstanceOf[FieldType], fieldPickle))
      case _ => compiletime.error("Could not find field name")
  
  inline private def writeOption[NamesAndElems <: Tuple](value: Any, pickle: format.PickleWriter, tag: format.FastTypeTag[?]): Unit =
    inline erasedValue[NamesAndElems] match
      case _: (Tuple2[name, tpe] *: tail) =>
        if value.isInstanceOf[tpe]
        then pickle.putChoice(value, tag.asInstanceOf, label[name])(
           p =>
             writeInl[tpe](value.asInstanceOf[tpe], p) 
        )
        else writeOption[tail](value, pickle, tag)
      case _: EmptyTuple => ()

  inline private def writeStruct[P, MirroredElemTypes <: Tuple, MirroredElemLabels <: Tuple](
    value: Any, pickle: format.PickleWriter, tag: format.FastTypeTag[?]): Unit =
    pickle.putStructure(value, tag.asInstanceOf)(writer =>
            writeFields[P, MirroredElemTypes, MirroredElemLabels](writer, value, 0))

  inline private def label[A]: String = constValue[A].asInstanceOf[String]

  /** Write a particular value to a pickle ready for it. Looks up given Writer. */
  inline private def writeInl[A](value: A, pickle: format.PickleWriter): Unit =
    summonFrom {
      case writer: Writer[A] => writer.write(value, pickle)
      // TODO - this is terrible.  We need to figure out a way to
      // manifest sub-writers for Enums/coproduct/sum types without
      // directly embedding them in the write method of the parent enum.
      case m: Mirror.ProductOf[A] =>
        writeStruct[A, m.MirroredElemTypes, m.MirroredElemLabels](
          value,
          pickle,
          format.fastTypeTag[A]())
      case m: Mirror.SumOf[A] =>
        writeOption[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]](
              value, 
              pickle,
              format.fastTypeTag[A]())
    }