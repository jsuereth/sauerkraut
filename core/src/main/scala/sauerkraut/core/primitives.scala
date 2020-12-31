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

given Writer[Unit] with
  val tag: format.FastTypeTag[Unit] = format.fastTypeTag[Unit]()
  def write(value: Unit, pickle: format.PickleWriter): Unit = pickle.writeUnit()
given Buildable[Unit] with
  override val tag = primitiveTag[Unit]()
  override def newBuilder: Builder[Unit] = 
    StaticValueBuilder(PrimitiveTag.UnitTag, ())
given Writer[Byte] with
  override val tag = primitiveTag[Byte]()
  override def write(value: Byte, pickle: format.PickleWriter): Unit =
    pickle.writeByte(value)
given Buildable[Byte] = PrimitiveBuildable[Byte]()
given Writer[Boolean] with
  override val tag = primitiveTag[Boolean]()
  override def write(value: Boolean, pickle: format.PickleWriter): Unit =
    pickle.writeBoolean(value)
given Buildable[Boolean] = PrimitiveBuildable[Boolean]()
given Writer[Char] with
  override val tag = primitiveTag[Char]()
  override def write(value: Char, pickle: format.PickleWriter): Unit =
    pickle.writeChar(value)
given Buildable[Char] = PrimitiveBuildable[Char]()
given Writer[Short] with
  override val tag = primitiveTag[Short]()
  override def write(value: Short, pickle: format.PickleWriter): Unit =
    pickle.writeShort(value)
given Buildable[Short] = PrimitiveBuildable[Short]()
given Writer[Int] with
  override val tag = primitiveTag[Int]()
  override def write(value: Int, pickle: format.PickleWriter): Unit =
    pickle.writeInt(value)
given Buildable[Int] = PrimitiveBuildable[Int]()
given Writer[Long] with
  override val tag = primitiveTag[Long]()
  override def write(value: Long, pickle: format.PickleWriter): Unit =
    pickle.writeLong(value)
given Buildable[Long] = PrimitiveBuildable[Long]()
given Writer[Float] with
  override val tag = primitiveTag[Float]()
  override def write(value: Float, pickle: format.PickleWriter): Unit =
    pickle.writeFloat(value)
given Buildable[Float] = PrimitiveBuildable[Float]()
given Writer[Double] with
  override val tag = primitiveTag[Double]()
  override def write(value: Double, pickle: format.PickleWriter): Unit =
    pickle.writeDouble(value)
given Buildable[Double] = PrimitiveBuildable[Double]()
given Writer[String] with
  override val tag = primitiveTag[String]()
  override def write(value: String, pickle: format.PickleWriter): Unit =
    pickle.writeString(value)
given Buildable[String] = PrimitiveBuildable[String]()
