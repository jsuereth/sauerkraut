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

import core.{Reader,Writer}
import format.{
  PickleFormat,
  PickleReader,
  PickleReaderSupport,
  PickleWriter,
  PickleWriterSupport
}

final class PickleFormatDsl[F <: PickleFormat](format: F)
  /** Applies a specific output to the pickle format chosen. */
  def to[O](output: O)(given s: PickleWriterSupport[O, F]): WriterDsl =
    WriterDsl(s.writerFor(format, output))
  def from[I](input: I)(given s: PickleReaderSupport[I, F]): ReaderDsl =
    ReaderDsl(s.readerFor(format, input))

final class ReaderDsl(pickle: PickleReader)
  def read[T](given s: Reader[T]): T =
    s.read(pickle)

final class WriterDsl(pickle: PickleWriter)
  /** Writes the given value into a pickle, and flushes the writer. */
  def write[T](value: T)(given s: Writer[T]): Unit =
    s.write(value, pickle)
    pickle.flush()
  /** Writes the given value into a pickle. */
  def lazyWrite[T](value: T)(given s: Writer[T]): Unit =
    s.write(value, pickle)

def pickle[F <: PickleFormat](format: F): PickleFormatDsl[F] = 
  PickleFormatDsl(format)

