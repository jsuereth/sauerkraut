package sauerkraut

import org.junit.Test
import org.junit.Assert._
import format.json.Json
import format.json.{given _}
import format.{fastTypeTag, primitiveTag}
import core.{Writer, Buildable, given _}
import java.io.StringWriter


case class TestManual(x: Double, b: Int, stuff: Array[Int])
given Writer[TestManual]:
  override def write(value: TestManual, pickle: format.PickleWriter): Unit =
    pickle.putStructure(value, fastTypeTag[TestManual]())(
      _.putField("x", w => w.putPrimitive(value.x, primitiveTag[Double]())).
      putField("b", w => w.putPrimitive(value.b, primitiveTag[Int]())).
      putField("stuff", _.putCollection(value.stuff.length)(c =>
        value.stuff.foreach(i => c.putElement(w => w.putPrimitive(i, primitiveTag[Int]()))))))

case class TestDerived(x: Double, b: Int, z: List[String])
  derives Writer, Buildable

class TestJson:

  def json[T: Writer](value: T): String =
     val out = StringWriter()
     pickle(Json).to(out).write(value)
     out.toString()

  @Test def writeUnit(): Unit =
    assertEquals("null", json(()))
  @Test def writeBoolean(): Unit =
    assertEquals("true", json(true))
    assertEquals("false", json(false))
  @Test def writeChar(): Unit =
    assertEquals("\"c\"", json('c'))
  @Test def writeShort(): Unit =
    assertEquals("1", json(1.toShort))
  @Test def writeInt(): Unit =  
    assertEquals("5", json(5))
  @Test def writeLong(): Unit =
    assertEquals("4", json(4L))
  @Test def writeFloat(): Unit =
    assertEquals("1.0", json(1.0f))
  @Test def writeDouble(): Unit =
    assertEquals("1.0", json(1.0))
  @Test def writeString(): Unit =
    assertEquals("\"Test\"", json("Test"))
  @Test def writeListOfInt(): Unit =
    assertEquals("""[3,4]""", json(List(3,4)))
  @Test def writeManualGiven(): Unit =
    assertEquals("""{"x":4.3,"b":1,"stuff":[1,2]}""",
                 json(TestManual(4.3, 1, Array(1,2))))
  @Test def writeDerivedCaseClass(): Unit =
    assertEquals("""{"x":1.2,"b":1,"z":["a","xyz"]}""", json(TestDerived(1.2, 1, List("a", "xyz"))))
