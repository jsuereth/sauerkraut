package sauerkraut
package core

import org.junit.Test
import org.junit.Assert._

case class EmptyMessage() derives Buildable
case class SimpleStruct(x: Int, y: String) derives Buildable

enum SimpleChoice derives Buildable
  case Test
  case SomethingOfValue(x: Int)

class TestBuildableBuiltInsAndDerived

  private inline def testPrimitive[T: Buildable](value: T): Unit =
    val b = summon[Buildable[T]].newBuilder
    b.putPrimitive(value)
    assertEquals(value, b.result)

  @Test def testPrimitives(): Unit =
     testPrimitive(true)
     testPrimitive(false)
     testPrimitive(4.toByte)
     testPrimitive(689.toShort)
     testPrimitive(-1)
     testPrimitive(10L)
     testPrimitive(1.0f)
     testPrimitive(1.0)
     testPrimitive("Hello, world")

  @Test def testCollections(): Unit =
    val b = summon[Buildable[Array[Int]]].newBuilder
    b.putElement().putPrimitive(1)
    b.putElement().putPrimitive(2)
    assertArrayEquals(Array(1,2), b.result)

  @Test def testEmptyMessage(): Unit =
    val b = summon[Buildable[EmptyMessage]].newBuilder
    assertEquals(EmptyMessage(), b.result)

  @Test def testSimpleStruct(): Unit =
    val b = summon[Buildable[SimpleStruct]].newBuilder
    b.putField("x").putPrimitive(1)
    b.putField("y").putPrimitive("to")
    assertEquals(SimpleStruct(1, "to"), b.result)

  @Test def testSimpleChoiceOfStruct(): Unit =
    val b = summon[Buildable[SimpleChoice]].newBuilder
    b.putChoice("SomethingOfValue").putField("x").putPrimitive(5)
    assertEquals(SimpleChoice.SomethingOfValue(5), b.result)

  @Test def testSimpleChoiceOfValue(): Unit =
    val b2 = summon[Buildable[SimpleChoice]].newBuilder
    b2.putChoice("Test")
    // Something is borked here.
    assertEquals(SimpleChoice.Test, b2.result)