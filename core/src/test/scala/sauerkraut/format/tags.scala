package sauerkraut.format

import org.junit.Test
import org.junit.Assert._

case class SimpleType()
case class ParameterizedType[T, U]()

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
    assertEquals(NonPrimitiveTag.Struct[Any]("sauerkraut.format.SimpleType"), 
                 fastTypeTag[SimpleType]())
    assertEquals(NonPrimitiveTag.Struct[Any]("sauerkraut.format.ParameterizedType[scala.Boolean, sauerkraut.format.SimpleType]"), 
                 fastTypeTag[ParameterizedType[Boolean, SimpleType]]())
  // TODO - Make sure List[T] + Array[T] work.