package sauerkraut
package format
package json


import org.typelevel.jawn.ast

class JsonConformanceTests extends testing.ComplianceTests
  override protected def roundTripImpl[T](
      writer: PickleWriter => Unit,
      reader: PickleReader => T): T =
    val out = java.io.StringWriter()
    writer(pickle(Json).to(out))
    out.flush()
    reader(pickle(Json).from(out.toString))