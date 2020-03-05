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

package test

import sauerkraut.core.{
  Buildable,
  Writer,
  given
}
import sauerkraut.format.pb.{
  Protos,
  TypeDescriptorMapping,
  field,
  given
}

case class TestPickle1(
  thing: Long @field(1),
  more: String @field(3),
  stuff: List[Int] @field(5)) 
  derives Writer, Buildable, TypeDescriptorMapping

case class NestedTestPickle2(
  field: TestPickle1 @field(3)
) derives Writer, Buildable, TypeDescriptorMapping

val TestProtos = Protos[TestPickle1 *: NestedTestPickle2 *: Unit]()