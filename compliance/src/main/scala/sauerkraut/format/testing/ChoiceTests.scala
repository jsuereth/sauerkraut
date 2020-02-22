package sauerkraut
package format
package testing

import core.{Writer, Buildable, given}

import org.junit.Test


enum Color derives Writer, Buildable
  case Red,Green,Blue

enum HasStuff derives Writer, Buildable
  case One(x: Int)
  case Two(y: String)

/** Ensures choice/enums can be serialized. */
trait ChoiceComplianceTests extends ComplianceTestBase
  @Test def testSimpleEnum(): Unit =
    roundTrip(Color.Red)
    roundTrip(Color.Green)
    roundTrip(Color.Blue)
  @Test def testEnumWithArguments(): Unit =
    roundTrip(HasStuff.One(1))
    roundTrip(HasStuff.Two("two"))