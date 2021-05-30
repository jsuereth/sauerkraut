package sauerkraut
package core
package testwriter

import org.junit.Test
// import org.junit.Assert._
import sauerkraut.format.PickleWriter
import sauerkraut.format.FastTypeTag
import sauerkraut.format.Struct
import sauerkraut.format.PickleStructureWriter


case class EmptyMessage() derives Writer
case class SimpleStruct(x: Int, y: String) derives Writer


import org.easymock.EasyMockSupport
import org.easymock.EasyMock

class TestWriterBuiltInsAndDervied extends EasyMockSupport:
  @Test def testEmptyMessage(): Unit =
    val data = EmptyMessage()
    val writer = summon[Writer[EmptyMessage]]
    val pickleMock: PickleWriter = mock(classOf[PickleWriter])
    val pickleStructureMock: PickleStructureWriter = mock(classOf[PickleStructureWriter])
    // Expected behavior
    EasyMock.expect(pickleMock.putStructure(
      EasyMock.anyObject(), //EasyMock.eq(data), 
      EasyMock.eq(writer.tag.asInstanceOf[Struct[?]]))(
        EasyMock.anyObject())).andAnswer(() => {
          val f = EasyMock.getCurrentArgument(2).asInstanceOf[PickleStructureWriter => Unit]
          f(pickleStructureMock)
          pickleMock
        });
    // Write to pickle.
    replayAll()
    writer.write(data, pickleMock)
    verifyAll()


  @Test def testSimpleStruct(): Unit =
    val data = SimpleStruct(1, "hi")
    val writer = summon[Writer[SimpleStruct]]
    val pickleMock: PickleWriter = mock(classOf[PickleWriter])
    val pickleStructureMock: PickleStructureWriter = mock(classOf[PickleStructureWriter])
    // Expected behavior
    EasyMock.expect(pickleMock.putStructure(
      EasyMock.anyObject(), //EasyMock.eq(data), 
      EasyMock.eq(writer.tag.asInstanceOf[Struct[?]]))(
        EasyMock.anyObject())).andAnswer(() => {
          val f = EasyMock.getCurrentArgument(2).asInstanceOf[PickleStructureWriter => Unit]
          f(pickleStructureMock)
          pickleMock
        });
    // Expect first field write
    EasyMock.expect(
      pickleStructureMock.putField(EasyMock.eq(1), EasyMock.eq("x"), EasyMock.anyObject())
    ).andAnswer(() => {
      val f = EasyMock.getCurrentArgument(2).asInstanceOf[PickleWriter => Unit]
      f(pickleMock)
      pickleStructureMock
    })
    EasyMock.expect(pickleMock.putInt(1)).andReturn(pickleMock)

    // Expect second field write
    EasyMock.expect(
      pickleStructureMock.putField(EasyMock.eq(2), EasyMock.eq("y"), EasyMock.anyObject())
    ).andAnswer(() => {
      val f = EasyMock.getCurrentArgument(2).asInstanceOf[PickleWriter => Unit]
      f(pickleMock)
      pickleStructureMock
    })
    EasyMock.expect(pickleMock.putString("hi")).andReturn(pickleMock)

    // Write to pickle.
    replayAll()
    writer.write(data, pickleMock)
    verifyAll()