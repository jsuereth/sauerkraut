package sauerkraut
package format
package xml


import org.junit.Test
import org.junit.Assert._
import format.{fastTypeTag, primitiveTag}
import core.{Writer, Buildable, given}
import java.io.StringWriter

case class TestDerived(x: Double, b: Int, z: List[String]) 
  derives Writer, Buildable

class TestJson

  def xml[T: Writer](value: T): String =
     val out = StringWriter()
     pickle(Xml).to(out).write(value)
     out.toString()

  @Test def writeUnit(): Unit =
    assertEquals("<primitive>null</primitive>", xml(()))
  @Test def writeBoolean(): Unit =
    assertEquals("<primitive>true</primitive>", xml(true))
    assertEquals("<primitive>false</primitive>", xml(false))
  @Test def writeChar(): Unit =
    assertEquals("<primitive>c</primitive>", xml('c'))
  @Test def writeShort(): Unit =
    assertEquals("<primitive>1</primitive>", xml(1.toShort))
  @Test def writeInt(): Unit =  
    assertEquals("<primitive>5</primitive>", xml(5))
  @Test def writeLong(): Unit =
    assertEquals("<primitive>4</primitive>", xml(4L))
  @Test def writeFloat(): Unit =
    assertEquals("<primitive>1.0</primitive>", xml(1.0f))
  @Test def writeDouble(): Unit =
    assertEquals("<primitive>1.0</primitive>", xml(1.0))
  @Test def writeString(): Unit =
    assertEquals("<primitive>Test</primitive>", xml("Test"))
  @Test def writeListOfInt(): Unit =
    assertEquals("""<collection><element><primitive>3</primitive></element><element><primitive>4</primitive></element></collection>""", xml(List(3,4)))
  @Test def writeDerivedCaseClass(): Unit =
    assertEquals("<structure>"+
    "<field name=\"x\"><primitive>1.2</primitive></field>"+
    "<field name=\"b\"><primitive>1</primitive></field>"+
    "<field name=\"z\"><collection>"+
      "<element><primitive>a</primitive></element>" +
      "<element><primitive>xyz</primitive></element>"+
    "</collection></field></structure>", xml(TestDerived(1.2, 1, List("a", "xyz"))))

