package sauerkraut
package core
package internal


import MacroHelper.{fieldNum, fieldNumToConstructorOrder}
import org.junit.Test
import org.junit.Assert._

case class MyTestClass(
  value: Int,
  value2: Int @Field(4),
  value3: Double,
  value4: Int @Field(2),
  value5: Int)


class TestMacroHelper:
  @Test def testFieldNumGen(): Unit =
    assertEquals(1, fieldNum[MyTestClass]("value"))
    assertEquals(4, fieldNum[MyTestClass]("value2"))
    assertEquals(5, fieldNum[MyTestClass]("value3"))
    assertEquals(2, fieldNum[MyTestClass]("value4"))
    assertEquals(6, fieldNum[MyTestClass]("value5"))


  @Test def testFieldNumToConstructorOrder(): Unit =
    assertEquals(0, fieldNumToConstructorOrder[MyTestClass](1))
    assertEquals(1, fieldNumToConstructorOrder[MyTestClass](4))
    assertEquals(2, fieldNumToConstructorOrder[MyTestClass](5))
    assertEquals(3, fieldNumToConstructorOrder[MyTestClass](2))
    assertEquals(4, fieldNumToConstructorOrder[MyTestClass](6))
