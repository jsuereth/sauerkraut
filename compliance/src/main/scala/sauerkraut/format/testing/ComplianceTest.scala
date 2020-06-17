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

import org.junit.Assert._
import sauerkraut.core.{Buildable,Writer,given _}

/** Basic tests a format can use to determine if they are
 * in compliance with the base spec.
 */
trait ComplianceTestBase:
  /**
   * This method should:
   * - Instantiate a format-specific [[PickleWriter]]
   * - call `writer` with this instance
   * - Construct a format-specific [[PickleReader]] which
   *   reads the results of the previous step.
   * - Call reader with the [[PickleReader]].
   * - Return the result of the lambda as the function result.
   */
  protected def roundTripImpl[T](writer: PickleWriter => Unit,
                                 reader: PickleReader => T): T

  /** A test to make sure a value is read back exactly as it is written. */
  final def roundTrip[A : Buildable : Writer](value: A): Unit =
    val result = roundTripImpl[A](
        summon[Writer[A]].write(value, _),
        (reader) => reader.push(summon[Buildable[A]].newBuilder).result
    )
    // TODO - error should include the TYPE.
    value match
      // B.S. to handle all the other array types for JUnit.
      case _: Array[Object] => assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Object]], 
                                                 result.asInstanceOf[Array[Object]])
      case _: Array[Byte] => assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Byte]], 
                                                 result.asInstanceOf[Array[Byte]])
      case _: Array[Char] =>  assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Char]], 
                                                 result.asInstanceOf[Array[Char]])
      case _: Array[Short] =>  assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Short]], 
                                                 result.asInstanceOf[Array[Short]])
      case _: Array[Int] =>  assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Int]], 
                                                 result.asInstanceOf[Array[Int]])
      case _: Array[Long] =>  assertArrayEquals(s"Failed to roundTrip: ${value}", 
                                                 value.asInstanceOf[Array[Long]], 
                                                 result.asInstanceOf[Array[Long]])
      // TODO - special case floating point numbers.
      case _ => assertEquals(s"Failed to roundTrip: ${value}", value, result)


  /** A test to ensure that write a value, altering the defintion and reading a new value
   *  should yield a 'conformant' result.
   * @param input  The original value to write.
   * @param output The value you'd like to receive asusming an evolution of the
   *               underlying type occured.
   */
  final def roundTripEvolution[A : Writer, B: Buildable](input: A, output: B): Unit =
     val result = roundTripImpl[B](
         summon[Writer[A]].write(input, _),
         (reader) => reader.push(summon[Buildable[B]].newBuilder).result
     )
     assertEquals(s"Failed to run evolution from $input to $output", output, result)

  /** A test to ensure that writing a value in a previous binary and reading it
   * in the current binary will work as expected.
   * @param value The value you expect to read.
   * @param writer A hand-written serialization mechanism meant to look like a previous
   *               version.
   */
  final def advanceRoundTripEvolution[A : Buildable](value: A)(writer: PickleWriter => Unit): Unit =
    val result = roundTripImpl(writer,  (reader) => reader.push(summon[Buildable[A]].newBuilder).result)
    assertEquals(s"Failed to run evolution on $value", value, result)

trait ComplianceTests 
  extends CollectionComplianceTests
  with PrimitiveComplianceTests
  with StructureComplianceTests
  with ChoiceComplianceTests

// TODO - evolution compliance tests
// 1. Field => Option[Field]
// 2. Field => Collection[Field]
// 3. Field => Nothing
// 4. Nothing => Field
// 5. ADT => New Class