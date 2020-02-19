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
import com.google.protobuf.{CodedInputStream,CodedOutputStream}

/** 
 * A binary format that is protocol-buffer like, but will not allow
 * any bi-directional encoding with actual protocol buffers.
 */ 
object RawBinary extends PickleFormat

given [O <: OutputStream] as PickleWriterSupport[O, RawBinary.type]
  def writerFor(format: RawBinary.type, output: O): PickleWriter = 
    RawBinaryPickleWriter(CodedOutputStream.newInstance(output))

given [I <: InputStream] as PickleReaderSupport[I, RawBinary.type]
  def readerFor(format: RawBinary.type, input: I): PickleReader = 
    RawBinaryPickleReader(CodedInputStream.newInstance(input))


given PickleWriterSupport[Array[Byte], RawBinary.type]
  def writerFor(format: RawBinary.type, output: Array[Byte]): PickleWriter = 
    RawBinaryPickleWriter(CodedOutputStream.newInstance(output))

given PickleReaderSupport[Array[Byte], RawBinary.type]
  def readerFor(format: RawBinary.type, input: Array[Byte]): PickleReader =
    RawBinaryPickleReader(CodedInputStream.newInstance(new java.io.ByteArrayInputStream(input)))

given PickleWriterSupport[ByteBuffer, RawBinary.type]
  def writerFor(format: RawBinary.type, output: ByteBuffer): PickleWriter = 
    RawBinaryPickleWriter(CodedOutputStream.newInstance(output))

given PickleReaderSupport[ByteBuffer, RawBinary.type]
  def readerFor(format: RawBinary.type, input: ByteBuffer): PickleReader =
    RawBinaryPickleReader(CodedInputStream.newInstance(input))

/**
 * A binary format that allows the encoding of specific protocol
 * buffers.  This does allow bi-directional encoding with actual
 * protocol buffer messages.
 * 
 * Note:  Only those used in construction of this class will be
 *        serializable.
 */ 
trait Protos extends PickleFormat
  def repository: TypeDescriptorRepository
object Protos
  inline def apply[T <: Tuple](): Protos =
     new Protos() {
       val repository = TypeDescriptorRepository[T]()
     }

given [O <: OutputStream, P <: Protos] as PickleWriterSupport[O, P]
  def writerFor(protos: P, output: O): PickleWriter =
    DescriptorBasedProtoWriter(CodedOutputStream.newInstance(output), protos.repository)

given [I <: InputStream, P <: Protos] as PickleReaderSupport[I, P]
  def readerFor(protos: P, input: I): PickleReader =
    ???