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

package sauerkraut.format


// Marker trait for defining pickle formats.
trait PickleFormat

/** A marker that a particular pickle format supports reading from a given input type. */
trait PickleReaderSupport[Input, Format]:
  /** Constructor something which can read the input type with the given pickle format. */
  def readerFor(format: Format, value: Input): PickleReader

/** A marker that a particular pickle format supports writing to a given output type. */
trait PickleWriterSupport[Output, Format]:
  /** Constructs a writer of the format instance to the output. */
  def writerFor(format: Format, output: Output): PickleWriter

