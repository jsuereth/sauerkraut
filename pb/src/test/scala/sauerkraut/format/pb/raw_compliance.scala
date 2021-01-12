package sauerkraut
package format
package pb

import java.io.{ByteArrayInputStream,ByteArrayOutputStream}


// We only do partial complaince, so we opt-in to what we can do.
class RawBinaryComplianceTests 
    extends testing.ComplianceTests:
  override protected def roundTripImpl[T](
       writer: PickleWriter => Unit,
       reader: PickleReader => T): T =
    val out = ByteArrayOutputStream()
    val w = pickle(RawBinary).to(out)
    writer(w)
    w.flush()
    reader(pickle(RawBinary).from(out.toByteArray))
