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

import core.{given _}

import org.junit.Test


/** Ensures collections can be serialized. */
trait CollectionComplianceTests extends ComplianceTestBase:
   @Test def writeListOfInt(): Unit =
     roundTrip(List(3,4,8))
   @Test def writeArrayOfString(): Unit =
     roundTrip(Array("Hi", "you", "guys", "I", "am", "an", "array"))
   @Test def writeArrayOfByte(): Unit =
     roundTrip(Array[Byte](1.toByte, 4.toByte))
   @Test def writeArrayOfLong(): Unit =
     roundTrip(Array(5L, 2144215323462462456L))
   @Test def writeIterableOfChar(): Unit =
     roundTrip(Iterable('a', 'f', '0'))