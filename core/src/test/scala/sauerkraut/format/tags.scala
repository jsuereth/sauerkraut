package sauerkraut.format

import sauerkraut.core.{Writer,given}
import org.junit.Test
import org.junit.Assert._

import collection.mutable.ArrayBuffer

case class SimpleType()
case class ParameterizedType[T, U]()

enum Adt:
  case Expr(x: Int)
  case Op

class TestFastTypeTag: 
  @Test def findsPrimitives(): Unit =
    assertEquals(PrimitiveTag.UnitTag, fastTypeTag[Unit]())
    assertEquals(PrimitiveTag.BooleanTag, fastTypeTag[Boolean]())
    assertEquals(PrimitiveTag.CharTag, fastTypeTag[Char]())
    assertEquals(PrimitiveTag.ShortTag, fastTypeTag[Short]())
    assertEquals(PrimitiveTag.IntTag, fastTypeTag[Int]())
    assertEquals(PrimitiveTag.LongTag, fastTypeTag[Long]())
    assertEquals(PrimitiveTag.FloatTag, fastTypeTag[Float]())
    assertEquals(PrimitiveTag.DoubleTag, fastTypeTag[Double]())
    assertEquals(PrimitiveTag.StringTag, fastTypeTag[String]())

  @Test def findStructs(): Unit =
    fastTypeTag[SimpleType]() match
      case s: Struct[_] =>
        assertEquals(s.name, "sauerkraut.format.SimpleType")
        assertEquals(s.fields.length, 0)
      case other => fail(s"$other is not a Struct!")
    fastTypeTag[ParameterizedType[Boolean, SimpleType]]() match
      case s: Struct[_] =>
        assertEquals(s.name, "sauerkraut.format.ParameterizedType[scala.Boolean, sauerkraut.format.SimpleType]")
        assertEquals(s.fields.length, 0)
      case other => fail(s"$other is not a Struct!")

  @Test def findSums(): Unit =
    fastTypeTag[Adt]() match
      case c: Choice[_] => assertEquals(c.name, "sauerkraut.format.Adt")
      case other => fail(s"$other is not a Choice!")

  @Test def findCollections(): Unit =
    val example = collectionTag[Array[Int], Int](fastTypeTag[Int]())
    assertEquals(fastTypeTag[Int](), example.elementTag)
    assertEquals("[I", example.name)

    val example2 = collectionTag[ArrayBuffer[Float], Float](fastTypeTag[Float]())
    assertEquals(fastTypeTag[Float](), example2.elementTag)
    assertEquals("scala.collection.mutable.ArrayBuffer", example2.name)

    assertEquals(
      collectionTag[Array[Int], Int](fastTypeTag[Int]()),
      fastTypeTag[Array[Int]]()
    )
    assertEquals(
      collectionTag[Seq[Int], Int](fastTypeTag[Int]()),
      fastTypeTag[Seq[Int]]()
    )
    assertEquals(
      collectionTag[Iterable[Int], Int](fastTypeTag[Int]()),
      fastTypeTag[Iterable[Int]]()
    )
    // Note: List[T] is actually a SUM type.  we may want to special case that one..
    assertFalse("Inequality works", fastTypeTag[Array[Int]]() == fastTypeTag[Array[Boolean]]())
    assertFalse("Inequality works", fastTypeTag[Array[Int]]() == fastTypeTag[Array[Float]]())

  @Test def primtiiveArrays(): Unit =
    assertTrue(summon[Writer[Array[Boolean]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Char]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Short]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Int]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Long]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Float]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertTrue(summon[Writer[Array[Double]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)
    assertFalse(summon[Writer[List[Double]]].tag.asInstanceOf[CollectionTag[_,_]].isArray)