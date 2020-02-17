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
import scala.collection.mutable.Builder

// TODO - make generic for all collections. Maybe codegen?
/** A writer for all collections extending Iterable. */
final class CollectionWriter[T: Writer]() extends Writer[Iterable[T]]
  override def write(value: Iterable[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.size)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )
/** A writer for raw array types. */
final class ArrayWriter[T: Writer : reflect.ClassTag] extends Writer[Array[T]]
  override def write(value: Array[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.length)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )

given [T](using Writer[T]) as Writer[List[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Seq[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Iterable[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T], reflect.ClassTag[T]) as Writer[Array[T]] = ArrayWriter[T]()

/** A reader for all collections that support the builder pattern. */
final class CollectionReader[E: Reader, To](b: () => Builder[E, To])
    extends Reader[To]
  override def read(pickle: PickleReader): To =
    pickle.readCollection(b(), summon[Reader[E]].read)

given [T](using Reader[T]) as Reader[List[T]] =
  CollectionReader[T, List[T]](() => List.newBuilder)
given [T](using Reader[T]) as Reader[Seq[T]] =
  CollectionReader[T, Seq[T]](() => Seq.newBuilder)
given [T](using Reader[T]) as Reader[Iterable[T]] =
  CollectionReader[T, Iterable[T]](() => Iterable.newBuilder)
given [T](using Reader[T], reflect.ClassTag[T]) as Reader[Array[T]] =
  CollectionReader[T, Array[T]](() => Array.newBuilder[T])