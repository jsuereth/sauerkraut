package sauerkraut
package format
package json

import java.io.StringWriter

object Json extends PickleFormat


given PickleWriterSupport[StringWriter, Json.type]
  def writerFor(format: Json.type, output: StringWriter): PickleWriter = 
    JsonPickleWriter(output)
