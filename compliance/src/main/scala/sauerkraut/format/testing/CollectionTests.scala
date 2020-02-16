package sauerkraut
package format
package testing

import core.{given}

import org.junit.Test


/** Ensures collections can be serialized. */
trait CollectionComplianceTests extends ComplianceTestBase
   @Test def writeListOfInt(): Unit =
     roundTrip(List(3,4,8))