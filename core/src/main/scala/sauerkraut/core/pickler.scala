package sauerkraut
package core

/**
 * A pickler is something that can read and write values of `T` to pickles.
 * 
 * This trait is used to ensure a specific type can be both read and written.
 */
trait Pickler[T] extends Reader[T] with Writer[T]

object Pickler
  // Helper class to combine readers + writers into pickles.
  private class BuiltPickler[T](r: Reader[T], w: Writer[T]) extends Pickler[T]
    override def read(pickle: format.PickleReader): T = r.read(pickle)
    override def write(value: T, pickle: format.PickleWriter): Unit = w.write(value, pickle)
    override def toString(): String = s"BuiltPickler($r, $w)"
  /** Provides picklers by joining readers + writers. */
  given [T](using Reader[T], Writer[T]) as Pickler[T] =
    BuiltPickler(summon[Reader[T]], summon[Writer[T]])
  