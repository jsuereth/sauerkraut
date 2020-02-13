package sauerkraut.format

import org.junit.Test
import org.junit.Assert._

class SimpleType
class ParameterizedType[T, U]

class TestFastTypeTag 
  @Test def findsPrimitives(): Unit =
    assertEquals(FastTypeTag.UnitTag, fastTypeTag[Unit]())
    assertEquals(FastTypeTag.BooleanTag, fastTypeTag[Boolean]())
    assertEquals(FastTypeTag.CharTag, fastTypeTag[Char]())
    assertEquals(FastTypeTag.ShortTag, fastTypeTag[Short]())
    assertEquals(FastTypeTag.IntTag, fastTypeTag[Int]())
    assertEquals(FastTypeTag.LongTag, fastTypeTag[Long]())
    assertEquals(FastTypeTag.FloatTag, fastTypeTag[Float]())
    assertEquals(FastTypeTag.DoubleTag, fastTypeTag[Double]())
    assertEquals(FastTypeTag.StringTag, fastTypeTag[String]())
  @Test def findNamed(): Unit =
    assertEquals(FastTypeTag.Named[Any]("sauerkraut.format.SimpleType"), 
                 fastTypeTag[SimpleType]())
    assertEquals(FastTypeTag.Named[Any]("sauerkraut.format.ParameterizedType[scala.Boolean, sauerkraut.format.SimpleType]"), 
                 fastTypeTag[ParameterizedType[Boolean, SimpleType]]())