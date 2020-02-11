package sauerkraut

import core.Writer
import format.{PickleFormat, PickleWriterSupport, PickleWriter}

// Helper API.
final class WriterApi[F <: PickleFormat](format: F)
  def to[O](output: O)(given s: PickleWriterSupport[O, F]): WriterApi2 =
    WriterApi2(s.writerFor(output))

// Helper API.
final class WriterApi2(pickle: PickleWriter)
  def write[T](value: T)(given s: Writer[T]): Unit =
    s.write(value, pickle)

def pickle[F <: PickleFormat](format: F): WriterApi[F] = WriterApi(format)