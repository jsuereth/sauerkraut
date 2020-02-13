package sauerkraut

import core.Writer
import format.{PickleFormat, PickleWriterSupport, PickleWriter}

// Helper API.
final class WriterApi[F <: PickleFormat](format: F)
  /** Applies a specific output to the pickle format chosen. */
  def to[O](output: O)(given s: PickleWriterSupport[O, F]): WriterApi2 =
    WriterApi2(s.writerFor(format, output))

// Helper API.
final class WriterApi2(pickle: PickleWriter)
  /** Writes the given value into a pickle, and flushes the writer. */
  def write[T](value: T)(given s: Writer[T]): Unit =
    s.write(value, pickle)
    pickle.flush()
  /** Writes the given value into a pickle. */
  def lazyWrite[T](value: T)(given s: Writer[T]): Unit =
    s.write(value, pickle)

def pickle[F <: PickleFormat](format: F): WriterApi[F] = WriterApi(format)