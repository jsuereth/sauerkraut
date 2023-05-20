package sauerkraut
package format
package pb

import java.io.{ByteArrayInputStream,ByteArrayOutputStream}


// We only do partial complaince, so we opt-in to what we can do.
class ProtoComplianceTests 
    extends testing.ComplianceTests:
  override protected def roundTripImpl[T](
       writer: PickleWriter => Unit,
       reader: PickleReader => T): T =
    val out = ByteArrayOutputStream()
    val w = pickle(Proto).to(out)
    writer(w)
    w.flush()
    try
      reader(pickle(Proto).from(out.toByteArray))
    catch
      case ex: Exception =>
        throw RuntimeException(s"Failed to deserialize [${prettyString(writer)}]\n from array\n: ${hexString(out.toByteArray)}", ex)


def prettyString(writer: PickleWriter => Unit): String =
  val w = java.io.StringWriter()
  writer(pretty.PrettyPrintPickleWriter(w))
  w.toString()