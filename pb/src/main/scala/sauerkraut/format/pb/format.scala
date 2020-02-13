package sauerkraut
package format
package pb

import java.nio.ByteBuffer
import java.io.OutputStream
import com.google.protobuf.CodedOutputStream

/** 
 * A binary format that is protocol-buffer like, but will not allow
 * any bi-directional encoding with actual protocol buffers.
 */ 
object RawBinary extends PickleFormat

given PickleWriterSupport[OutputStream, RawBinary.type]
  def writerFor(output: OutputStream): PickleWriter = 
    RawProtocolBufferPickleWriter(CodedOutputStream.newInstance(output))

given PickleWriterSupport[Array[Byte], RawBinary.type]
  def writerFor(output: Array[Byte]): PickleWriter = 
    RawProtocolBufferPickleWriter(CodedOutputStream.newInstance(output))

given PickleWriterSupport[ByteBuffer, RawBinary.type]
  def writerFor(output: ByteBuffer): PickleWriter = 
    RawProtocolBufferPickleWriter(CodedOutputStream.newInstance(output))

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

// TODO constructor for Protos
given PickleWriterSupport[OutputStream, Protos]
  def writerFor(output: OutputStream): PickleWriter =
    ???

