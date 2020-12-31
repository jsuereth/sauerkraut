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

import format.{
  PickleReader,
  PickleCollectionWriter,
  FastTypeTag,
  CollectionTag,
  collectionTag,
  fastTypeTag
}
import scala.collection.mutable.{
  Builder => ScalaCollectionBuilder,
  ArrayBuffer
}

// TODO - make generic for all collections. Maybe codegen?
/** A writer for all collections extending Iterable. */
final class GenCollectionWriter[T: Writer, C <: Iterable[T]](
  override val tag: CollectionTag[C, T]
) extends CollectionWriter[C]:
  private val elWriter = summon[Writer[T]]
  override def writeCollection(value: C, pickle: PickleCollectionWriter): Unit =
    pickle.sizeHint(value.size)
    for item <- value
    do pickle.writeElement(item)(using elWriter)
/** A writer for raw array types. */
final class ArrayWriter[T: Writer : reflect.ClassTag](
  override val tag: CollectionTag[Array[T], T]
) extends CollectionWriter[Array[T]]:
  private val elWriter = summon[Writer[T]]
  override def writeCollection(value: Array[T], pickle: PickleCollectionWriter): Unit =
    pickle.sizeHint(value.length)
    for item <- value
    do pickle.writeElement(item)(using elWriter)

given [T](using Writer[T]): Writer[List[T]] = 
  GenCollectionWriter[T, List[T]](collectionTag[List[T], T](summon[Writer[T]].tag))
given [T](using Writer[T]): Writer[Vector[T]] = 
  GenCollectionWriter[T, Vector[T]](collectionTag[Vector[T], T](summon[Writer[T]].tag))
given [T](using Writer[T]): Writer[Seq[T]] = 
  GenCollectionWriter[T, Seq[T]](collectionTag[Seq[T], T](summon[Writer[T]].tag))
given [T](using Writer[T]): Writer[Iterable[T]] = 
  GenCollectionWriter[T, Iterable[T]](collectionTag[Iterable[T], T](summon[Writer[T]].tag))
given [T](using Writer[T]): Writer[collection.mutable.ArrayBuffer[T]] = 
  GenCollectionWriter[T, collection.mutable.ArrayBuffer[T]](collectionTag[collection.mutable.ArrayBuffer[T], T](summon[Writer[T]].tag))
given [T](using Writer[T], reflect.ClassTag[T]): Writer[Array[T]] =
  ArrayWriter[T](collectionTag[Array[T], T](summon[Writer[T]].tag))

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
