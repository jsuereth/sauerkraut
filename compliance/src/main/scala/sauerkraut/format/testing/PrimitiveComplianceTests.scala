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

