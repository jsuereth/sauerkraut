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
package json

import java.io.{Reader,Writer, File}
import org.typelevel.jawn.ast

object Json extends PickleFormat


given [O <: Writer] as PickleWriterSupport[O, Json.type]
  override def writerFor(format: Json.type, output: O): PickleWriter = 
    JsonPickleWriter(output)

given PickleReaderSupport[File, Json.type]
  override def readerFor(format: Json.type, input: File): PickleReader =
    JsonReader(ast.JParser.parseFromFile(input).get)

given PickleReaderSupport[String, Json.type]
  override def readerFor(format: Json.type, input: String): PickleReader =
    JsonReader(ast.JParser.parseUnsafe(input))

// TODO - more reader options.