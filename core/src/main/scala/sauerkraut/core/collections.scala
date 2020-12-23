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

import format.{PickleReader,PickleWriter}
import scala.collection.mutable.{
  Builder => ScalaCollectionBuilder,
  ArrayBuffer
}

/** A marker trait denoting how to write a collection of similar values. */
sealed trait CollectionWriter[E, C] extends Writer[C]

// TODO - make generic for all collections. Maybe codegen?
/** A writer for all collections extending Iterable. */
final class GenCollectionWriter[T: Writer]() extends Writer[Iterable[T]]:
  override def write(value: Iterable[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.size)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )
/** A writer for raw array types. */
final class ArrayWriter[T: Writer : reflect.ClassTag] extends Writer[Array[T]]:
  override def write(value: Array[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.length)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )

given [T](using Writer[T]): Writer[List[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): Writer[Vector[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): Writer[Seq[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): Writer[Iterable[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): Writer[collection.mutable.ArrayBuffer[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T], reflect.ClassTag[T]): Writer[Array[T]] = ArrayWriter[T]()

final class SimpleCollectionBuilder[E: Buildable, To](
    b: ScalaCollectionBuilder[E, To])
    extends CollectionBuilder[E, To]:
  private var tmpBuilder = List.newBuilder[Builder[E]]
  def putElement(): Builder[E] =
    val nextElement = summon[Buildable[E]].newBuilder
    tmpBuilder += nextElement
    nextElement
  def result: To =
    // TODO - make efficient?
    b ++= (tmpBuilder.result.map(_.result))
    b.result
  override def toString: String = s"Builder[$b]"

final class CollectionBuildable[E: Buildable, To](
    newColBuilder: () => ScalaCollectionBuilder[E, To])
    extends Buildable[To]:
  def newBuilder: Builder[To] =
    SimpleCollectionBuilder[E, To](newColBuilder())

given [T](using Buildable[T]): Buildable[List[T]] =
  CollectionBuildable[T, List[T]](() => List.newBuilder)
given [T](using Buildable[T]): Buildable[Seq[T]] =
  CollectionBuildable[T, Seq[T]](() => Seq.newBuilder)
given [T](using Buildable[T]): Buildable[Iterable[T]] =
  CollectionBuildable[T, Iterable[T]](() => Iterable.newBuilder)
given [T](using Buildable[T]): Buildable[Vector[T]] =
  CollectionBuildable[T, Vector[T]](() => Vector.newBuilder)
given [T](using Buildable[T], reflect.ClassTag[T]): Buildable[Array[T]] =
  CollectionBuildable[T, Array[T]](() => Array.newBuilder[T])
given [T](using Buildable[T], reflect.ClassTag[T]): Buildable[ArrayBuffer[T]] =
  CollectionBuildable[T, ArrayBuffer[T]](() => ArrayBuffer.newBuilder[T])