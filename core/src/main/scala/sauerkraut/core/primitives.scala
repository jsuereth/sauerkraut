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

import format.{fastTypeTag,FastTypeTag}

// A writer of primitive values.
final class PrimitiveWriter[T](tag: FastTypeTag[T]) extends Writer[T]
  override def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.putPrimitive(value, tag)

object PrimitiveWriter {
  inline def apply[T](): PrimitiveWriter[T] =
    new PrimitiveWriter[T](fastTypeTag[T]())
}

// A reader of primitive values.
final class PrimitiveReader[T](tag: FastTypeTag[T]) extends Reader[T]
  override def read(pickle: format.PickleReader): T =
    pickle.readPrimitive(tag).asInstanceOf[T]
object PrimitiveReader
  inline def apply[T](): PrimitiveReader[T] =
    new PrimitiveReader[T](fastTypeTag[T]())

given Writer[Unit] = PrimitiveWriter[Unit]()
given Reader[Unit] = PrimitiveReader[Unit]()
given Writer[Boolean] = PrimitiveWriter[Boolean]()
given Reader[Boolean] = PrimitiveReader[Boolean]()
given Writer[Char] = PrimitiveWriter[Char]()
given Reader[Char] = PrimitiveReader[Char]()
given Writer[Short] = PrimitiveWriter[Short]()
given Reader[Short] = PrimitiveReader[Short]()
given Writer[Int] = PrimitiveWriter[Int]()
given Reader[Int] = PrimitiveReader[Int]()
given Writer[Long] = PrimitiveWriter[Long]()
given Reader[Long] = PrimitiveReader[Long]()
given Writer[Float] = PrimitiveWriter[Float]()
given Reader[Float] = PrimitiveReader[Float]()
given Writer[Double] = PrimitiveWriter[Double]()
given Reader[Double] = PrimitiveReader[Double]()
given Writer[String] = PrimitiveWriter[String]()
given Reader[String] = PrimitiveReader[String]()
