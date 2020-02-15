package sauerkraut
package format
package testing

import core.{given}

import org.junit.Test

/** Tests round-trips on primitives within a format. */
trait PrimitiveComplianceTests extends ComplianceTestBase
  @Test def writeUnit(): Unit =
    roundTrip(())
  @Test def writeBoolean(): Unit =
    roundTrip(true)
    roundTrip(false)
  // TODO - test wierd cases, not simple ones.
  @Test def writeChar(): Unit =
    roundTrip('a')
    roundTrip('!')
  @Test def writeShort(): Unit =
    roundTrip(1.toShort)
  @Test def writeInt(): Unit =
    roundTrip(1)
  @Test def writeLong(): Unit =
    roundTrip(1L)
  @Test def writeFloat(): Unit =
    roundTrip(1.0f)
  @Test def writeDouble(): Unit =
    roundTrip(1.0)
  @Test def writeString(): Unit =
    roundTrip("")
    roundTrip("testing")

