package test

import org.junit.Test
import org.junit.Assert._
import sauerkraut.pickle
import sauerkraut.format.pb.given
import collection.JavaConverters.asScalaBufferConverter

class TestWriteThenRead
  @Test def sauerWritesGoogleReads(): Unit =
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