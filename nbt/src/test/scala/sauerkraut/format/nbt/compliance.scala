package sauerkraut
package format
package nbt

import java.io.{
  ByteArrayInputStream,
  ByteArrayOutputStream
}

class NbtComplianceTests extends testing.ComplianceTests:
  override protected def roundTripImpl[T](
      writer: PickleWriter => Unit,
      reader: PickleReader => T
  ): T =
    val out = ByteArrayOutputStream()
    val w = pickle(Nbt).to(out)
    writer(w)
    w.flush()
    reader(pickle(Nbt).from(ByteArrayInputStream(out.toByteArray)))

