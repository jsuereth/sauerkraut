package sauerkraut.format

import sauerkraut.core.{given}
import org.junit.Test
import org.junit.Assert._

case class SimpleType()
case class ParameterizedType[T, U]()

enum Adt
  case Expr(x: Int)
  case Op

class TestFastTypeTag 
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
    assertEquals(
      Given[Any]("scala.collection.immutable.Seq[scala.Int]"),
      fastTypeTag[Seq[Int]]()
    )
    assertEquals(
      Given[Any]("scala.collection.Iterable[scala.Int]"),
      fastTypeTag[Iterable[Int]]()
    )
    assertEquals(
      Given[Any]("scala.Array[scala.Int]"),
      fastTypeTag[Array[Int]]()
    )
    // Note: List[T] is actually a SUM type.  we may want to special case that one..