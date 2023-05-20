package sauerkraut
package format
package testing

import org.junit.Test
import sauerkraut.core.{Buildable, Writer, given}
import scala.collection.mutable.ArrayBuffer

object FieldTests:
  // Nested structures with non-standard field numbers
  case class SimpleMessage(value: Int @Field(2), message: String @Field(1))
    derives Writer, Buildable
  case class LargerMessage(
    messages: ArrayBuffer[SimpleMessage] @Field(1),
    otherNums: ArrayBuffer[Double] @Field(2),
    ints: ArrayBuffer[Long] @Field(3)
  ) derives Writer, Buildable

/** Compliance tests that check serialization using @Field annotation. */
trait FieldTests extends ComplianceTestBase:
  @Test def testFieldOutOfOrder(): Unit =
    import FieldTests.SimpleMessage
    roundTrip(SimpleMessage(231415325, "A test of out of order"))
  @Test def testNestedStructureWithFieldNumbers(): Unit =
    import FieldTests.{LargerMessage, SimpleMessage}
    roundTrip(
      LargerMessage(
        messages = ArrayBuffer(
          SimpleMessage(1124312542, "This is a test of simple byte serialization for us all"),
          SimpleMessage(0, ""),
          SimpleMessage(-1, "ANother string")),
        otherNums = ArrayBuffer(1.0, -0.000001, 1000000000000000.0101010),
        ints = ArrayBuffer(1,2,3,4,5,-1,-2,-4,1425,0)
      )
    )
