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
final class PrimitiveWriter[T](tag: PrimitiveTag[T]) extends Writer[T]:
  override def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.putPrimitive(value, tag)

object PrimitiveWriter {
  inline def apply[T](): PrimitiveWriter[T] =
    new PrimitiveWriter[T](primitiveTag[T]())
}

// A builder of primitive values.
final class SimplePrimitiveBuilder[T](
    override val tag: PrimitiveTag[T]) 
    extends PrimitiveBuilder[T]:
  private var value: Option[T] = None
  override def putPrimitive(p: T): Unit =
    value = Some(p)
  override def result: T = 
    value match
      case Some(v) => v
      case None =>
        // TODO - do we use a default here and ALLOW missing primtiive values? 
        throw WriteException("Did not find value for primitive!", null)
  override def toString(): String =
    s"Builder[$tag]"

final class PrimitiveBuildable[T](override val tag: PrimitiveTag[T]) extends Buildable[T]:
  override def newBuilder: Builder[T] = SimplePrimitiveBuilder[T](tag)

object PrimitiveBuildable:
  inline def apply[T](): Buildable[T] =
    new PrimitiveBuildable[T](primitiveTag[T]())

final class StaticValueBuilder[T](
    override val tag: PrimitiveTag[T],
    override val result: T) 
    extends PrimitiveBuilder[T]:
  override def putPrimitive(value: T): Unit = ()

given Writer[Unit] = PrimitiveWriter[Unit]()
given Buildable[Unit] with
  override val tag: format.FastTypeTag[Unit] = format.fastTypeTag[Unit]()
  override def newBuilder: Builder[Unit] = 
    StaticValueBuilder(PrimitiveTag.UnitTag, ())
given Writer[Byte] = PrimitiveWriter[Byte]()
given Buildable[Byte] = PrimitiveBuildable[Byte]()
given Writer[Boolean] = PrimitiveWriter[Boolean]()
given Buildable[Boolean] = PrimitiveBuildable[Boolean]()
given Writer[Char] = PrimitiveWriter[Char]()
given Buildable[Char] = PrimitiveBuildable[Char]()
given Writer[Short] = PrimitiveWriter[Short]()
given Buildable[Short] = PrimitiveBuildable[Short]()
given Writer[Int] = PrimitiveWriter[Int]()
given Buildable[Int] = PrimitiveBuildable[Int]()
given Writer[Long] = PrimitiveWriter[Long]()
given Buildable[Long] = PrimitiveBuildable[Long]()
given Writer[Float] = PrimitiveWriter[Float]()
given Buildable[Float] = PrimitiveBuildable[Float]()
given Writer[Double] = PrimitiveWriter[Double]()
given Buildable[Double] = PrimitiveBuildable[Double]()
given Writer[String] = PrimitiveWriter[String]()
given Buildable[String] = PrimitiveBuildable[String]()
