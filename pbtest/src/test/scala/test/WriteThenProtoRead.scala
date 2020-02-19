package test

import org.junit.Test
import org.junit.Assert._
import sauerkraut.{pickle, write}
import sauerkraut.format.pb.given
import collection.JavaConverters.asScalaBufferConverter

class TestWriteThenRead
  @Test def sauerWritesGoogleReadsSimple(): Unit =
    val source = TestPickle1(2, "test", List(3,5))
    val out = java.io.ByteArrayOutputStream()
    pickle(TestProtos).to(out).write(source)
    val result = Test1OuterClass.Test1.parseFrom(
        new java.io.ByteArrayInputStream(out.toByteArray))
    assertEquals(source.thing, result.getThing)
    assertEquals(source.more, result.getMore)
    // TODO - Remove warnings from this.
    assertEquals(source.stuff,
                 result.getStuffList.asScala.map(_.intValue))
  @Test def sauerWritesGoogleReadsNested(): Unit =
    val source = NestedTestPickle2(TestPickle1(2L, "hello", List(1,2,3)))
    val out = java.io.ByteArrayOutputStream()
    pickle(TestProtos).to(out).write(source)
    val result = Test1OuterClass.NestedTest2.parseFrom(
      java.io.ByteArrayInputStream(out.toByteArray))
    assertEquals(source.field.thing, result.getField.getThing)
    assertEquals(source.field.more, result.getField.getMore)
    assertEquals(source.field.stuff, result.getField.getStuffList.asScala.map(_.intValue))