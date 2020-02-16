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

import java.io.StringWriter
import org.typelevel.jawn.ast

object Json extends PickleFormat


given PickleWriterSupport[StringWriter, Json.type]
  def writerFor(format: Json.type, output: StringWriter): PickleWriter = 
    JsonPickleWriter(output)

given PickleReaderSupport[String, Json.type]
  def readerFor(format: Json.type, input: String): PickleReader =
    JsonReader(ast.JParser.parseUnsafe(input))

// TODO - more reader options.