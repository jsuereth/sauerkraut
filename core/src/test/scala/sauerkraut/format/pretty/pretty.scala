package sauerkraut
package format
package pretty
package testing

import core.{Writer,given _}
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

enum SimpleAdt derives Writer:
  case One
  case Two(x: Int)

def (in: String) fixLineEnd: String =
  in.replaceAll("\r\n", "\n")

class TestPrettyPrint {
  @Test def testEnum(): Unit =
    assertEquals("""|Choice(sauerkraut.format.pretty.testing.SimpleAdt) {
                    |  One: Struct(sauerkraut.format.pretty.testing.SimpleAdt.One.type) {}
                    |}""".stripMargin('|').fixLineEnd, SimpleAdt.One.prettyPrint.fixLineEnd)
    assertEquals("""|Choice(sauerkraut.format.pretty.testing.SimpleAdt) {
                    |  Two: Struct(sauerkraut.format.pretty.testing.SimpleAdt.Two) {
                    |    x: 2
                    |  }
                    |}""".stripMargin('|').fixLineEnd, SimpleAdt.Two(2).prettyPrint.fixLineEnd)
  @Test def testStruct(): Unit =
    assertEquals("""|Struct(sauerkraut.format.pretty.testing.TestStruct) {
                    |  value: 20
                    |  floater: 0.5
                    |}""".stripMargin('|').fixLineEnd, TestStruct(20, 0.5).prettyPrint.fixLineEnd)

  @Test def testStructOfStruct(): Unit =
    assertEquals("""|Struct(sauerkraut.format.pretty.testing.StructOfStruct) {
                    |  name: Hi
                    |  value: Struct(sauerkraut.format.pretty.testing.TestStruct) {
                    |    value: 20
                    |    floater: 0.5
                    |  }
                    |}""".stripMargin('|').fixLineEnd, StructOfStruct("Hi", TestStruct(20, 0.5)).prettyPrint.fixLineEnd)
}

