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

import core.{Writer, Buildable, given _}

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
) derives Writer, Buildable

case class StructureOfStructures(
  a: Int,
  b: TestSimpleStructureOfPrimitives
) derives Writer, Buildable

case class StructureOfCollections(
    a: Int,
    z: List[String]
) derives Writer, Buildable

case class Simple(x: String) derives Writer, Buildable

case class StructureOfCollectionOfStructures(
  a: Int,
  b: List[Simple]
) derives Writer, Buildable

/** Ensures collections can be serialized. */
trait StructureComplianceTests extends ComplianceTestBase:
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
  @Test def testStructureOfStrucutres(): Unit =
    roundTrip(StructureOfStructures(
        4,
        TestSimpleStructureOfPrimitives(
        true,
        'a',
        2.toShort,
        5,
        6L,
        1.0f,
        1.0,
        "Hello")
    ))
  @Test def testStructureOfCollections(): Unit =
    roundTrip(StructureOfCollections(
        4,
        List("one", "two", "three")
    ))
  @Test def testStructureOfCollectionOfStructures(): Unit =
    roundTrip(StructureOfCollectionOfStructures(4, List(Simple("Hi"), Simple("You"))))