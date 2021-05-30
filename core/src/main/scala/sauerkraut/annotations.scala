/*
 * Copyright 2021 Google
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

// A set of annotations we use to guide compilation/synthesis of pickling format code.


/** 
 * An annotation denoting the integer representation of a field on a case class.
 * 
 * By default, case class fields are assigned a number based on their order in
 * the definition of the case class.  For Example:
 * 
 * {{{
 * case class Message(x: Int, y: String, z: Double)
 * }}}
 * 
 * Would see x @ 1, y @ 2 and z @ 3, for field numbers.  This annotation allows
 * you to customize the number assigned to a field.   If you want total control,
 * you can use this annotation against ALL fields in the class.
 * 
 * {{{
 * case class Message4(x: Int @field(2), y: String @field(1), z: Double @field(5))
 * }}}
 * 
 * Here, we'd have x @ 2, y @ 1 and z @ 5.  Note: Field numbers do not need to be
 * contiguous, only unique amongst fields of  a case class.
 * 
 * It is possible to blend manual field numbers with implciitly defined ones.
 * 
 * TODO - Describe algoirthm:
 * - Undefined fields will be given the earliest possible number allowed
 * - Define skips + migration strategy.
 */
case class Field(number: Int) extends scala.annotation.StaticAnnotation