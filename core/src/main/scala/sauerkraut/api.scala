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

import core.{Writer,Buildable}
import format.{
  PickleFormat,
  PickleReader,
  PickleReaderSupport,
  PickleWriter,
  PickleWriterSupport
}


/** API for pickling. */
def pickle[F <: PickleFormat](format: F): PickleFormatDsl[F] = 
  PickleFormatDsl(format)

/** Helper class to simplify lining up a PickleFormat with input/output + values. */
final class PickleFormatDsl[F <: PickleFormat](format: F)
  /** Applies a specific output to the pickle format chosen. */
  def to[O](output: O)(given s: PickleWriterSupport[O, F]): PickleWriter =
    s.writerFor(format, output)
  def from[I](input: I)(given s: PickleReaderSupport[I, F]): PickleReader =
    s.readerFor(format, input)


/** Reads type `T` using the PickleReader. */
def [T](pickle: PickleReader) read(given b: core.Buildable[T]): T =
  pickle.push(b.newBuilder).result


/** Writes the value to a pickle. Note: This flushes the pickle writer. */
def [T](pickle: PickleWriter) write(value: T)(given s: core.Writer[T]): Unit =
  s.write(value, pickle)
  pickle.flush()

/** Writes the value to a pickle.  Note: This does not flush the pickle writer. */
def [T](pickle: PickleWriter) lazyWrite(value: T)(given s: core.Writer[T]): Unit =
  s.write(value, pickle)

