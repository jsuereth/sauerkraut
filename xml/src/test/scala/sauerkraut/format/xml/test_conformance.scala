package sauerkraut
package format
package xml


class JsonConformanceTests extends testing.ComplianceTests:
  override protected def roundTripImpl[T](
      writer: PickleWriter => Unit,
      reader: PickleReader => T): T =
    val out = java.io.StringWriter()
    writer(pickle(Xml).to(out))
    out.flush()
    reader(pickle(Xml).from(out.toString))