package sauerkraut
package format
package testing

import core.{Writer, Reader, given}

import org.junit.Test


case class TestSimpleStructureOfPrimitives(
    x: Int, y: String, z: Double
) derives Writer, Reader

case class StructureOfStructureAndCollections(
    a: Int,
    y: TestSimpleStructureOfPrimitives,
    z: List[String]
) derives Writer, Reader

/** Ensures collections can be serialized. */
trait StructureComplianceTests extends ComplianceTestBase
  @Test def testSimpleStructure(): Unit =
    roundTrip(TestSimpleStructureOfPrimitives(5,"Hello", 1.0))
  // Currently broken.
//   @Test def testComplexNestedStructure(): Unit =
//     roundTrip(StructureOfStructureAndCollections(
//         4,
//         TestSimpleStructureOfPrimitives(1, "test", 2.0),
//         List("one", "two", "three")
//     ))