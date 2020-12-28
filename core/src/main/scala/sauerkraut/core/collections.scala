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

import format.{PickleReader,PickleWriter, FastTypeTag}
import scala.collection.mutable.{
  Builder => ScalaCollectionBuilder,
  ArrayBuffer
}

/** A marker trait denoting how to write a collection of similar values. */
sealed trait CollectionWriter[C] extends Writer[C]

// TODO - make generic for all collections. Maybe codegen?
/** A writer for all collections extending Iterable. */
final class GenCollectionWriter[T: Writer]() extends CollectionWriter[Iterable[T]]:
  override def write(value: Iterable[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.size)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )
/** A writer for raw array types. */
final class ArrayWriter[T: Writer : reflect.ClassTag] extends CollectionWriter[Array[T]]:
  override def write(value: Array[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.length)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )

given [T](using Writer[T]): CollectionWriter[List[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): CollectionWriter[Vector[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): CollectionWriter[Seq[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): CollectionWriter[Iterable[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T]): CollectionWriter[collection.mutable.ArrayBuffer[T]] = GenCollectionWriter[T]().asInstanceOf
given [T](using Writer[T], reflect.ClassTag[T]): CollectionWriter[Array[T]] = ArrayWriter[T]()

final class SimpleCollectionBuilder[E: Buildable, To](
    override val tag: format.CollectionTag[To, E],
    b: ScalaCollectionBuilder[E, To])
    extends CollectionBuilder[E, To]:
  private var tmpBuilder = collection.mutable.ArrayBuffer.newBuilder[Builder[E]]
  def sizeHint(length: Int): CollectionBuilder[E, To] =
    tmpBuilder.sizeHint(length)
    this
  def putElement(): Builder[E] =
    val nextElement = summon[Buildable[E]].newBuilder
    tmpBuilder += nextElement
    nextElement
  def result: To =
    val buffer = tmpBuilder.result
    b.sizeHint(buffer.size)
    val i = buffer.iterator
    while (i.hasNext)
      b += i.next.result
    b.result
  override def toString: String = s"Builder[$b]"

trait CollectionBuildable[C] extends Buildable[C]:
  def tag: FastTypeTag[C]

final class GenCollectionBuildable[E: Buildable, To](
    myTag: format.CollectionTag[To, E],
    newColBuilder: () => ScalaCollectionBuilder[E, To])
    extends CollectionBuildable[To]:
  def tag: FastTypeTag[To] = myTag
  def newBuilder: Builder[To] =
    SimpleCollectionBuilder[E, To](myTag, newColBuilder())

given [T](using Buildable[T]): CollectionBuildable[List[T]] =
  GenCollectionBuildable[T, List[T]](
    format.collectionTag[List[T], T](summon[Buildable[T]].tag),
    () => List.newBuilder)
given [T](using Buildable[T]): CollectionBuildable[Seq[T]] =
  GenCollectionBuildable[T, Seq[T]](
    format.collectionTag[Seq[T], T](summon[Buildable[T]].tag),
    () => Seq.newBuilder)
given [T](using Buildable[T]): CollectionBuildable[Iterable[T]] =
  GenCollectionBuildable[T, Iterable[T]](
    format.collectionTag[Iterable[T], T](summon[Buildable[T]].tag),
    () => Iterable.newBuilder)
given [T](using Buildable[T]): CollectionBuildable[Vector[T]] =
  GenCollectionBuildable[T, Vector[T]](
    format.collectionTag[Vector[T], T](summon[Buildable[T]].tag),
    () => Vector.newBuilder)
given [T](using Buildable[T], reflect.ClassTag[T]): CollectionBuildable[ArrayBuffer[T]] =
  GenCollectionBuildable[T, ArrayBuffer[T]](
    format.collectionTag[ArrayBuffer[T], T](summon[Buildable[T]].tag),
    () => ArrayBuffer.newBuilder[T])
given [T](using Buildable[T], reflect.ClassTag[T]): CollectionBuildable[Array[T]] =
  GenCollectionBuildable[T, Array[T]](
    format.collectionTag[Array[T], T](summon[Buildable[T]].tag),
    () => Array.newBuilder[T])
