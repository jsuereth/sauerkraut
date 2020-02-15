package sauerkraut
package format
package pb

import java.io.{ByteArrayInputStream,ByteArrayOutputStream}
import com.google.protobuf.{CodedInputStream,CodedOutputStream}


class RawBinaryComplianceTests extends testing.ComplianceTests
  override protected def roundTripImpl[T](
       writer: PickleWriter => Unit,
       reader: PickleReader => T): T =
    val out = ByteArrayOutputStream()
    val w = RawBinaryPickleWriter(CodedOutputStream.newInstance(out))
    writer(w)
    w.flush()
    val r = RawBinaryPickleReader(CodedInputStream.newInstance(
        ByteArrayInputStream(out.toByteArray)
    ))
    reader(r)
