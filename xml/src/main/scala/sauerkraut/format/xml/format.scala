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
package xml

import java.io.{Reader,Writer,File,InputStream}
import java.nio.{ByteBuffer}
import java.nio.channels.ReadableByteChannel


object Xml extends PickleFormat

given [O <: Writer] as PickleWriterSupport[O, Xml.type]
  override def writerFor(format: Xml.type, output: O): PickleWriter = 
    XmlPickleWriter(output)

given [I <: InputStream] as PickleReaderSupport[I, Xml.type]
  override def readerFor(format: Xml.type, input: I): PickleReader =
    XmlReader(inputStreamSaxReader(input))

given PickleReaderSupport[String, Xml.type]
  override def readerFor(format: Xml.type, input: String): PickleReader =
    XmlReader(inputStreamSaxReader(java.io.ByteArrayInputStream(input.getBytes)))
