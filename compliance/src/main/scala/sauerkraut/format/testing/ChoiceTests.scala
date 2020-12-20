/*
 * Copyright 2019 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sauerkraut
package format
package testing

import core.{Writer, Buildable, given}

import org.junit.Test


enum Color derives Writer, Buildable:
  case Red,Green,Blue

enum HasStuff derives Writer, Buildable:
  case One(x: Int)
  case Two(y: String)

/** Ensures choice/enums can be serialized. */
trait ChoiceComplianceTests extends ComplianceTestBase:
  @Test def testSimpleEnum(): Unit =
    roundTrip(Color.Red)
    roundTrip(Color.Green)
    roundTrip(Color.Blue)
  @Test def testEnumWithArguments(): Unit =
    roundTrip(HasStuff.One(1))
    roundTrip(HasStuff.Two("two"))