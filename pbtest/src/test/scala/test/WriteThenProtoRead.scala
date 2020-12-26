package test

import org.junit.{Test, Ignore}
import org.junit.Assert._
import sauerkraut.{read, pickle, write}
import sauerkraut.format.pb.{given}
import collection.JavaConverters.asScalaBufferConverter

class TestWriteThenRead:
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

  @Test def googleWritesSauerReadsNested(): Unit =
    val source = 
      Test1OuterClass.NestedTest2.newBuilder
      .setField(
        Test1OuterClass.Test1.newBuilder
        .setThing(2)
        .setMore("test")
        .build
      )
      .build
    val out = java.io.ByteArrayOutputStream()
    source.writeTo(out)
    val in = java.io.ByteArrayInputStream(out.toByteArray)
    val result = pickle(TestProtos).from(in).read[NestedTestPickle2]
    assertEquals(source.getField.getThing, result.field.thing)
    assertEquals(source.getField.getMore, result.field.more)
  @Test def googleWritesSauerReadsPackedRepeated(): Unit =
    val source = 
      Test1OuterClass.NestedTest2.newBuilder
      .setField(
        Test1OuterClass.Test1.newBuilder
        .setThing(2)
        .setMore("test")
        .addStuff(1)
        .addStuff(2)
        .build
      )
      .build
    val out = java.io.ByteArrayOutputStream()
    source.writeTo(out)
    val in = java.io.ByteArrayInputStream(out.toByteArray)
    val result = pickle(TestProtos).from(in).read[NestedTestPickle2]
    assertEquals(source.getField.getThing, result.field.thing)
    assertEquals(source.getField.getMore, result.field.more)
    assertEquals(source.getField.getStuffList.asScala.map(_.intValue), result.field.stuff)


  @Test def sauerWritesGoogleReadsPackedRepeated(): Unit =
    val source = 
      NestedTestPickle2(
        TestPickle1(2, "test", List(1, 2))
      )
    val out = java.io.ByteArrayOutputStream()
    pickle(TestProtos).to(out).write(source)
    val result = Test1OuterClass.NestedTest2.parseFrom(
      java.io.ByteArrayInputStream(out.toByteArray))
    assertEquals(source.field.thing, result.getField.getThing)
    assertEquals(source.field.more, result.getField.getMore)
    assertEquals(source.field.stuff, result.getField.getStuffList.asScala.map(_.intValue))