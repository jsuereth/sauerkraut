package sauerkraut
package format
package pretty

object Pretty extends PickleFormat


given [O <: java.io.Writer] as PickleWriterSupport[O, Pretty.type]
  def writerFor(format: Pretty.type, output: O): PickleWriter =
    PrettyPrintPickleWriter(output)
