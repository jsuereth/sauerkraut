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

/**
 * A pickler is something that can read and write values of `T` to pickles.
 * 
 * This trait is used to ensure a specific type can be both read and written.
 */
trait Pickler[T] extends Buildable[T] with Writer[T]

/**
 * A special marker trait denoting a pickler that can write values of `T` composed
 * of more than on element of type `E`.
 * 
 * This can be used for complex collections, for example
 * `C = Map[String, Int]` and `E = (String, Int)`.
 * 
 * This is a special instance of a normal Pickler, and must be available for collections
 * to participate in hot-path optimisations in pickling formats.
 */
trait CollectionPickler[E, C] extends Pickler[C]

object Pickler:
  // Helper class to combine readers + writers into picklers.
  private class BuiltPickler[T](b: Buildable[T], w: Writer[T]) extends Pickler[T]:
    override def newBuilder: Builder[T] = b.newBuilder
    override def write(value: T, pickle: format.PickleWriter): Unit = w.write(value, pickle)
    override def toString(): String = s"BuiltPickler($b, $w)"
  // Helper class to combine collection readers and writes into picklers.
  private class BuiltCollectionPickler[E,C](b: CollectionBuildable[E, C], w: CollectionWriter[E, C]) extends Pickler[C]:
    override def newBuilder: Builder[C] = b.newBuilder
    override def write(value: C, pickle: format.PickleWriter): Unit = w.write(value, pickle)
    override def toString(): String = s"BuiltCollectionPickler($b, $w)"
  /** Provides picklers by joining readers + writers. */
  given [T](using Buildable[T], Writer[T]): Pickler[T] =
    BuiltPickler(summon[Buildable[T]], summon[Writer[T]])
  