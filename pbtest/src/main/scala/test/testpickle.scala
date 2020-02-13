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
  Writer,
  given
}
import sauerkraut.format.{
  fastTypeTag,
  FastTypeTag
}
import sauerkraut.format.pb.{
  Protos,
  TypeDescriptorMapping,
  TypeDescriptorRepository,
  given
}

case class TestPickle1(thing: Long, more: String, stuff: List[Int]) derives Writer

// TODO - autogenerate this somehow
// TODO - reduce boilerplate before autogenerating.
object TestPickle1Descriptor extends TypeDescriptorMapping[TestPickle1]
  def fieldDescriptor[F](name: String): Option[sauerkraut.format.pb.TypeDescriptorMapping[F]] =
    None
  def fieldNumber(name: String): Int =
    if (name == "thing") 1
    else if (name == "more") 3
    else if (name == "stuff") 5
    else ???

object TestProtos extends Protos
  object repository extends TypeDescriptorRepository
    val TestPickle1Tag = fastTypeTag[TestPickle1]()
    override def find[T](tag: FastTypeTag[T]): TypeDescriptorMapping[T] =
      tag match
        case TestPickle1Tag => TestPickle1Descriptor.asInstanceOf[TypeDescriptorMapping[T]]