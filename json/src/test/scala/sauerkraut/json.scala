package sauerkraut

import org.junit.Test
import org.junit.Assert._
import format.json.Json
import format.json.given
import format.fastTypeTag
import core.{Reader, Writer, given}
import java.io.StringWriter


case class TestManual(x: Double, b: Int, stuff: Array[Int])
given Writer[TestManual]
  override def write(value: TestManual, pickle: format.PickleWriter): Unit =
    pickle.beginStructure(value, fastTypeTag[TestManual]()).
      putField("x", w => w.putPrimitive(value.x, fastTypeTag[Double]())).
      putField("b", w => w.putPrimitive(value.b, fastTypeTag[Int]())).
      putField("stuff", w => {
        val c = w.beginCollection(value.stuff.length)
        value.stuff.foreach(i => c.putElement(w => w.putPrimitive(i, fastTypeTag[Int]())))
        c.endCollection()
      }).
      endStructure()

case class TestDerived(x: Double, b: Int) derives Writer, Reader

class TestJson {

  def json[T: Writer](value: T): String =
     val out = StringWriter()
     pickle(Json).to(out).write(value)
     out.toString()

  @Test def writeInt(): Unit =  
    assertEquals("5", json(5))
  @Test def writeManualGiven(): Unit =
    assertEquals("""{"x":4.3,"b":1,"stuff":[1,2]}""",
                 json(TestManual(4.3, 1, Array(1,2))))
  @Test def writeDerivedCaseClass(): Unit =
    assertEquals("""{"x":1.2,"b":1}""", json(TestDerived(1.2, 1)))
}