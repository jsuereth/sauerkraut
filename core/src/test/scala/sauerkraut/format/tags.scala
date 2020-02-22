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
    assertEquals(Struct[Any]("sauerkraut.format.SimpleType"), 
                 fastTypeTag[SimpleType]())
    assertEquals(Struct[Any]("sauerkraut.format.ParameterizedType[scala.Boolean, sauerkraut.format.SimpleType]"), 
                 fastTypeTag[ParameterizedType[Boolean, SimpleType]]())
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