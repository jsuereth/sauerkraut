package sauerkraut
package format
package pretty
package testing

import core.{Writer,given}
import org.junit.Test
import org.junit.Assert._

case class TestStruct(
  value: Int,
  floater: Double
) derives Writer
case class StructOfStruct(
    name: String,
    value: TestStruct
) derives Writer

class TestPrettyPrint {
  @Test def testStruct(): Unit =
    assertEquals("""|Struct(sauerkraut.format.pretty.testing.TestStruct) {
                    |  value: 20
                    |  floater: 0.5
                    |}""".stripMargin('|'), TestStruct(20, 0.5).prettyPrint)

  @Test def testStructOfStruct(): Unit =
    assertEquals("""|Struct(sauerkraut.format.pretty.testing.StructOfStruct) {
                    |  name: Hi
                    |  value: Struct(sauerkraut.format.pretty.testing.TestStruct) {
                    |    value: 20
                    |    floater: 0.5
                    |  }
                    |}""".stripMargin('|'), StructOfStruct("Hi", TestStruct(20, 0.5)).prettyPrint)
}

