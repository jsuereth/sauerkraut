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

import format.{primitiveTag,PrimitiveTag}

// A writer of primitive values.
final class PrimitiveWriter[T](tag: PrimitiveTag[T]) extends Writer[T]
  override def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.putPrimitive(value, tag)

object PrimitiveWriter {
  inline def apply[T](): PrimitiveWriter[T] =
    new PrimitiveWriter[T](primitiveTag[T]())
}

// A reader of primitive values.
final class PrimitiveReader[T](tag: PrimitiveTag[T]) extends Reader[T]
  override def read(pickle: format.PickleReader): T =
    pickle.readPrimitive(tag).asInstanceOf[T]
object PrimitiveReader
  inline def apply[T](): PrimitiveReader[T] =
    new PrimitiveReader[T](primitiveTag[T]())

// A builder of primitive values.
final class SimplePrimitiveBuilder[T] extends PrimitiveBuilder[T]
  private var value: Option[T] = None
  override def putPrimitive(p: T): Unit =
    value = Some(p)
  override def result: T = 
    value match
      case Some(v) => v
      case None => throw RuntimeException("Did not find value for primitive!")
final class PrimitiveBuildable[T] extends Buildable[T]
  override def newBuilder: Builder[T] = SimplePrimitiveBuilder[T]()

final class StaticValueBuilder[T](override val result: T) 
    extends PrimitiveBuilder[T]
  override def putPrimitive(value: T): Unit = ()


given Writer[Unit] = PrimitiveWriter[Unit]()
given Reader[Unit] = PrimitiveReader[Unit]()
given Buildable[Unit]
  override def newBuilder: Builder[Unit] = 
    StaticValueBuilder(())
given Writer[Byte] = PrimitiveWriter[Byte]()
given Reader[Byte] = PrimitiveReader[Byte]()
given Buildable[Byte] = PrimitiveBuildable[Byte]()
given Writer[Boolean] = PrimitiveWriter[Boolean]()
given Reader[Boolean] = PrimitiveReader[Boolean]()
given Buildable[Boolean] = PrimitiveBuildable[Boolean]()
given Writer[Char] = PrimitiveWriter[Char]()
given Reader[Char] = PrimitiveReader[Char]()
given Buildable[Char] = PrimitiveBuildable[Char]()
given Writer[Short] = PrimitiveWriter[Short]()
given Reader[Short] = PrimitiveReader[Short]()
given Buildable[Short] = PrimitiveBuildable[Short]()
given Writer[Int] = PrimitiveWriter[Int]()
given Reader[Int] = PrimitiveReader[Int]()
given Buildable[Int] = PrimitiveBuildable[Int]()
given Writer[Long] = PrimitiveWriter[Long]()
given Reader[Long] = PrimitiveReader[Long]()
given Buildable[Long] = PrimitiveBuildable[Long]()
given Writer[Float] = PrimitiveWriter[Float]()
given Reader[Float] = PrimitiveReader[Float]()
given Buildable[Float] = PrimitiveBuildable[Float]()
given Writer[Double] = PrimitiveWriter[Double]()
given Reader[Double] = PrimitiveReader[Double]()
given Buildable[Double] = PrimitiveBuildable[Double]()
given Writer[String] = PrimitiveWriter[String]()
given Reader[String] = PrimitiveReader[String]()
given Buildable[String] = PrimitiveBuildable[String]()
