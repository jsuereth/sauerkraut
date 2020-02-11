package sauerkraut
package core

import format.{fastTypeTag,FastTypeTag}

// A writer of primitive values.
final class PrimitiveWriter[T](tag: FastTypeTag[T]) extends Writer[T]
  override def write(value: T, pickle: format.PickleWriter): Unit =
    pickle.putPrimitive(value, tag)

object PrimitiveWriter {
  inline def apply[T](): PrimitiveWriter[T] = new PrimitiveWriter[T](fastTypeTag[T]())
}

// A reader of primitive values.
final class PrimitiveReader[T] extends Reader[T]
  override def read(pickle: format.PickleReader): T =
    pickle.readPrimitive().asInstanceOf[T]


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
