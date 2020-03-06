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
