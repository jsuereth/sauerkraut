package sauerkraut.format


// Marker trait for defining pickle formats.
trait PickleFormat


trait PickleReaderSupport[Input, Format]
  def readerFor(value: Input): PickleReader


trait PickleWriterSupport[Output, Format]
  def writerFor(output: Output): PickleWriter
