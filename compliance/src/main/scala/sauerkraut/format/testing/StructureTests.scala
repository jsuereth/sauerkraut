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
    x: Int, y: String, z: Double
) derives Writer, Reader

case class StructureOfStructureAndCollections(
    a: Int,
    y: TestSimpleStructureOfPrimitives,
    z: List[String]
) derives Writer, Reader

/** Ensures collections can be serialized. */
trait StructureComplianceTests extends ComplianceTestBase
  @Test def testSimpleStructure(): Unit =
    roundTrip(TestSimpleStructureOfPrimitives(5,"Hello", 1.0))
  // Currently broken.
//   @Test def testComplexNestedStructure(): Unit =
//     roundTrip(StructureOfStructureAndCollections(
//         4,
//         TestSimpleStructureOfPrimitives(1, "test", 2.0),
//         List("one", "two", "three")
//     ))