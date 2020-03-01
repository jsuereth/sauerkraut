package sauerkraut
package benchmarks

import java.io.{
  OutputStream,
  InputStream,
  Writer,
  Reader,
  InputStreamReader,
  OutputStreamWriter
}
import java.nio.ByteBuffer


class ByteBufferOutputStream(buffer: ByteBuffer) extends OutputStream
  override def write(i: Int): Unit =
    try buffer.put(i.toByte)
    catch
      case _: java.nio.BufferOverflowException =>
        throw RuntimeException(s"Reached the limit of $buffer")
  override def write(b: Array[Byte], off: Int, len: Int) =
    try buffer.put(b, off, len)
    catch
      case _: java.nio.BufferOverflowException =>
        throw RuntimeException(s"Reached the limit of $buffer")

class ByteBufferInputStream(buffer: ByteBuffer) extends InputStream
  override def read(): Int =
    if !buffer.hasRemaining() 
    then -1
    else buffer.get() & 0xFF

  override def read(bytes: Array[Byte], off: Int, len: Int): Int =
    if !buffer.hasRemaining()
    then -1
    else
      val length = Math.min(len, buffer.remaining)
      buffer.get(bytes, off, length)
      length

def (buffer: ByteBuffer) in: InputStream = ByteBufferInputStream(buffer)
def (buffer: ByteBuffer) reader: Reader = InputStreamReader(buffer.in)
def (buffer: ByteBuffer) out: OutputStream = ByteBufferOutputStream(buffer)
def (buffer: ByteBuffer) writer: Writer = OutputStreamWriter(buffer.out)
