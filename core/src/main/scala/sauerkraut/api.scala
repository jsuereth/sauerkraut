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