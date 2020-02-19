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
package format
package nbt

import java.io.{
  DataInputStream,
  DataOutputStream,
  InputStream,
  OutputStream
}

/**
 * A binary format used in some java video games.
 */
object Nbt extends PickleFormat

given [O <: OutputStream] as PickleWriterSupport[O, Nbt.type]
  def writerFor(format: Nbt.type, output: O): PickleWriter =
    NbtPickleWriter(internal.TagOutputStream(DataOutputStream(output)))

given [I <: InputStream] as PickleReaderSupport[I, Nbt.type]
  def readerFor(format: Nbt.type, input: I): PickleReader =
    NbtPickleReader(internal.TagInputStream(DataInputStream(input)))