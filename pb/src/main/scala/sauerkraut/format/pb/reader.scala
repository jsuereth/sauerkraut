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
package pb

import streams.{
  LimitableTagReadingStream,
  Tag,
  WireFormat
}

class DescriptorBasedProtoReader(in: LimitableTagReadingStream)
    extends PickleReader:
  def push[T](b: core.Builder[T]): core.Builder[T] =
    b match
      case b: core.StructureBuilder[T] => pushImpl(b)
      case _ => throw BuildException(s"Unable to deserialize proto to $b", null)

  private def pushImpl[T](b: core.Builder[T]): core.Builder[T] =
    b match
      case b: core.StructureBuilder[T] => readStructure(b)
      case b: core.CollectionBuilder[_,_] => pushImpl(b.putElement())
      case b: core.PrimitiveBuilder[_] => Shared.readPrimitive(in)(b)
      case _ => throw BuildException(s"Unsupported builder for protos: $b", null)
    b

  private inline def readField[T](fieldBuilder: core.Builder[T], wireType: WireFormat): Unit = 
    fieldBuilder match {
      case choice: core.ChoiceBuilder[T] => ???
      case struct: core.StructureBuilder[T] =>
        Shared.limitByWireType(in)(WireFormat.LengthDelimited) {
          readStructure(struct)
        }
      case col: core.CollectionBuilder[_,_] =>
        col.tag.elementTag match
          case x: PrimitiveTag[_] =>
            if WireFormat.LengthDelimited == wireType then
              Shared.readCompressedPrimitive(in)(col, x)
            else pushImpl(col.putElement())
          case other =>
            Shared.limitByWireType(in)(WireFormat.LengthDelimited) {
              pushImpl(col.putElement())
            }
      case p: core.PrimitiveBuilder[_] => Shared.readPrimitive(in)(p)
    }

  private def readStructure[T](struct: core.StructureBuilder[T]): Unit =
    var done: Boolean = false
    while !done do
      in.readTag() match
        case 0 => done = true
        case Tag(wireType, num) => readField(struct.putField(num), wireType)
