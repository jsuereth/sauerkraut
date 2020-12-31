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
  /** Writes this value into a pickle. */
  def write(value: T, pickle: format.PickleWriter): Unit
  /** Type tag for what this can write. */
  def tag: format.FastTypeTag[T]

/** A Writer for writing structured data of type T. */
abstract class StructureWriter[T] extends Writer[T]:
  /** Writes this structure into a pickle-for-structures. */
  def writeStructure(value: T, pickle: format.PickleStructureWriter): Unit
  final def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.writeStructure(value)(using this)

/** A Writer for writing a choice of options all of type T. */
abstract class ChoiceWriter[T] extends Writer[T]:
  /** Writes a choice value into a pickle-for-choices. */
  def writeChoice(value: T, pickle: format.PickleChoiceWriter): Unit
  final def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.writeChoice(value)(using this)

/** A writer for writing collections of values of type T. */
abstract class CollectionWriter[T] extends Writer[T]:
  /** Writes this collection into a pickle-for-collections. */
  def writeCollection(value: T, pickle: format.PickleCollectionWriter): Unit
  final def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.writeCollection(value)(using this)


object Writer:
  import scala.compiletime.{constValue,erasedValue,summonFrom}
  import deriving._
  import internal.InlineHelper.summonLabel
  /** Derives writers of type T. */
  inline def derived[T](using m: Mirror.Of[T]): Writer[T] =
    inline m match
      case m: Mirror.ProductOf[T] =>
        new StructureWriter[T]:
          override val tag: format.FastTypeTag[T] = format.fastTypeTag[T]()
          // TODO - we should pre-summon writers for every field type here.
          override def writeStructure(value: T, pickle: format.PickleStructureWriter): Unit =
            writeStruct[m.MirroredElemTypes, m.MirroredElemLabels](
              value,
              pickle,
              tag)
      case m: Mirror.SumOf[T] =>
        new ChoiceWriter[T]:
          override val tag: format.FastTypeTag[T] = format.fastTypeTag[T]()
          override def writeChoice(value: T, pickle: format.PickleChoiceWriter): Unit =
            // TODO - We may need to synthesize writers for each option.
            writeOption[Tuple.Zip[m.MirroredElemLabels, m.MirroredElemTypes]](
              value, 
              pickle,
              tag)
      case _ => compiletime.error("Cannot derive serialization for non-product classes")
  /** Writes all the fields (in Elems) to the structure writer. */
  inline private def writeElems[Elems <: Tuple, Labels <: Tuple](
    pickle: format.PickleStructureWriter, value: Any, idx: Int): Unit =
      inline erasedValue[Elems] match
        case _: (elem *: elems1) =>
          // TODO - Allow index to be specified via annotation.
          pickle.writeField[elem](
            idx, 
            summonLabel[Labels](idx),
            value.asInstanceOf[Product].productElement(idx).asInstanceOf[elem])(
              // TODO - more efficient handling of nested writer.
              using summonWriter[elem]
            )
          writeElems[elems1, Labels](pickle, value, idx+1)
        case _: EmptyTuple => ()
  
  inline private def writeOption[NamesAndElems <: Tuple](value: Any, pickle: format.PickleChoiceWriter, tag: format.FastTypeTag[?]): Unit =
    inline erasedValue[NamesAndElems] match
      case _: (Tuple2[name, tpe] *: tail) =>
        if value.isInstanceOf[tpe]
        then pickle.writeChoice[tpe](0, label[name], value.asInstanceOf[tpe])(using summonWriter[tpe])
        else writeOption[tail](value, pickle, tag)
      case _: EmptyTuple => ()

  inline private def writeStruct[MirroredElemTypes <: Tuple, MirroredElemLabels <: Tuple](
    value: Any, pickle: format.PickleStructureWriter, tag: format.FastTypeTag[?]): Unit =
    writeElems[MirroredElemTypes, MirroredElemLabels](pickle, value, 0)

  inline private def label[A]: String = constValue[A].asInstanceOf[String]

  inline private def summonWriter[A]: Writer[A] =
    summonFrom {
      case w: Writer[A] => w
      case s: Mirror.ProductOf[A] => derived[A]
      case s: Mirror.SumOf[A] => derived[A]
      case _ => noWriterFoundError[A]
    }

  import scala.quoted._  
  inline def noWriterFoundError[T]: Writer[T] = ${noWriterFoundErrorImpl[T]}
  private def noWriterFoundErrorImpl[T: Type](using qctx: Quotes): Expr[Writer[T]] =
    quotes.reflect.report.error(s"Could not find given Writer[T] for: ${Type.show[T]}")
    Expr(null)