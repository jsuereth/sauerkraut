package sauerkraut
package format
package json


import org.typelevel.jawn.ast

class JsonConformanceTests extends testing.ComplianceTests
  override protected def roundTripImpl[T](
      writer: PickleWriter => Unit,
      reader: PickleReader => T): T =
    val out = java.io.StringWriter()
    val w = JsonPickleWriter(out)
    writer(w)
    w.flush()
    val in = JsonReader(ast.JParser.parseUnsafe(out.toString))
    reader(in)