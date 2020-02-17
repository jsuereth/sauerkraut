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
class CollectionWriter[T: Writer]() extends Writer[Iterable[T]]
  override def write(value: Iterable[T], pickle: PickleWriter): Unit =
    pickle.putCollection(value.size)(c =>
      for item <- value
      do c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    )

given [T](using Writer[T]) as Writer[List[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Seq[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Iterable[T]] = CollectionWriter[T]().asInstanceOf

final class CollectionReader[E: Reader, To](b: () => Builder[E, To])
    extends Reader[To]
  def read(pickle: PickleReader): To =
    pickle.readCollection(b(), summon[Reader[E]].read)

given [T](using Reader[T]) as Reader[List[T]] =
  CollectionReader[T, List[T]](() => List.newBuilder)
given [T](using Reader[T]) as Reader[Seq[T]] =
  CollectionReader[T, Seq[T]](() => Seq.newBuilder)
given [T](using Reader[T]) as Reader[Iterable[T]] =
  CollectionReader[T, Iterable[T]](() => Iterable.newBuilder)
