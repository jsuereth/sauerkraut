package sauerkraut
package format
package pb

import java.io.{ByteArrayInputStream,ByteArrayOutputStream}
import com.google.protobuf.{CodedInputStream,CodedOutputStream}

// We only do partial complaince, so we opt-in to what we can do.
class RawBinaryComplianceTests extends testing.ComplianceTestBase
    with testing.PrimitiveComplianceTests
    with testing.CollectionComplianceTests
    // TODO - Fix structure writing for raw format w/ collections.
    with testing.StructureComplianceTests
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
