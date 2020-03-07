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



import org.junit.Test
import scala.collection.mutable.ArrayBuffer
import sauerkraut.core.{Buildable,Writer,given}



case class FieldToCollectionStart(
    value: Boolean
) derives Writer, Buildable

case class FieldToCollectionEnd(
    value: ArrayBuffer[Boolean]
) derives Writer, Buildable

/**
 * This test determines whether it is safe in a given format to convert a field from a specific type into
 * a collection of that type.
 */
trait FieldToCollectionEvolution extends ComplianceTestBase
   @Test def testReadsCollectionForSingle(): Unit =
     roundTripEvolution(FieldToCollectionStart(true), FieldToCollectionEnd(ArrayBuffer(true)))