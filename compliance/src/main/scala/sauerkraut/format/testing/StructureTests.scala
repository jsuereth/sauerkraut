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

import core.{Writer, Reader, given}

import org.junit.Test


case class TestSimpleStructureOfPrimitives(
    b: Boolean,
    c: Char,
    s: Short, 
    i: Int,
    l: Long, 
    f: Float, 
    z: Double,
    ss: String
) derives Writer, Reader

case class StructureOfCollections(
    a: Int,
    z: List[String]
) derives Writer, Reader

/** Ensures collections can be serialized. */
trait StructureComplianceTests extends ComplianceTestBase
  @Test def testSimpleStructure(): Unit =
    roundTrip(TestSimpleStructureOfPrimitives(
      true,
      'a',
      2.toShort,
      5,
      6L,
      1.0f,
      1.0,
      "Hello"))
  // Currently broken.
  @Test def testStructureOfCollections(): Unit =
    roundTrip(StructureOfCollections(
        4,
        List("one", "two", "three")
    ))