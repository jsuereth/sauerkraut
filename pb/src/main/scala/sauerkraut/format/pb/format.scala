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
package pb

import java.nio.ByteBuffer
import java.io.{InputStream,OutputStream}

/**
 * A binary format that allows the encoding of specific protocol
 * buffers.  This does allow bi-directional encoding with actual
 * protocol buffer messages.
 * 
 * Note:  Only those used in construction of this class will be
 *        serializable.
 */ 
object Proto extends PickleFormat
  
given [O <: OutputStream]: PickleWriterSupport[O, Proto.type] with
  def writerFor(protos: Proto.type, output: O): PickleWriter =
    ProtoWriter(streams.ProtoOutputStream(output))

given [I <: InputStream]: PickleReaderSupport[I, Proto.type] with
  def readerFor(protos: Proto.type, input: I): PickleReader =
    DescriptorBasedProtoReader(streams.ProtoInputStream(input))

given PickleReaderSupport[Array[Byte], Proto.type] with
  def readerFor(format: Proto.type, input: Array[Byte]): PickleReader =
    DescriptorBasedProtoReader(streams.ProtoInputStream(new java.io.ByteArrayInputStream(input)))