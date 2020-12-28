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

import com.google.protobuf.{
  CodedInputStream,
  WireFormat
}
import WireFormat.{
  WIRETYPE_LENGTH_DELIMITED
}

class DescriptorBasedProtoReader(in: CodedInputStream, repo: TypeDescriptorRepository)
    extends PickleReader:
  def push[T](b: core.Builder[T]): core.Builder[T] =
    b match
      case b: core.StructureBuilder[T] =>
        pushWithDesc(b, repo.find(b.tag))
      case _ => throw BuildException(s"Unable to deserialize proto to $b", null)

  private def pushWithDesc[T](b: core.Builder[T], desc: ProtoTypeDescriptor[T]): core.Builder[T] =
    try
      b match
        case b: core.StructureBuilder[T] => readStructure(b, desc.asInstanceOf[MessageProtoDescriptor[T]])
        case b: core.CollectionBuilder[_,_] => pushWithDesc(b.putElement(), desc.asInstanceOf[CollectionTypeDescriptor[_,_]].element.asInstanceOf)
        case b: core.PrimitiveBuilder[_] => Shared.readPrimitive(in)(b)
        case _ => throw BuildException(s"Unsupported builder for protos: $b", null)
    catch
      case e: ClassCastException => throw BuildException(s"Builder error.  Builder: $b, Descriptor: $desc", e)
    b

  private inline def readField[T](fieldBuilder: core.Builder[T], desc: ProtoTypeDescriptor[T], wireType: Int): Unit =
    try 
      fieldBuilder match {
        case choice: core.ChoiceBuilder[T] => ???
        case struct: core.StructureBuilder[T] =>
          Shared.limitByWireType(in)(WIRETYPE_LENGTH_DELIMITED) {
            readStructure(struct, desc.asInstanceOf)
          }
        case col: core.CollectionBuilder[_,_] =>
          val colDesc = desc.asInstanceOf[CollectionTypeDescriptor[_,_]]
          colDesc.element match
            case x: PrimitiveTypeDescriptor[_] =>
              if (wireType == WIRETYPE_LENGTH_DELIMITED) 
                Shared.readCompressedPrimitive(in)(col, x.tag.asInstanceOf)
              else pushWithDesc(col.putElement(), x.asInstanceOf)
            case other =>
              Shared.limitByWireType(in)(WIRETYPE_LENGTH_DELIMITED) {
                pushWithDesc(col.putElement(), other.asInstanceOf)
              }
        case p: core.PrimitiveBuilder[_] => Shared.readPrimitive(in)(p)
      }
    catch
      case e: ClassCastException => throw BuildException(s"Builder and descriptor do not align.  Builder: $fieldBuilder, Descriptor: $desc", null)

  private def readStructure[T](struct: core.StructureBuilder[T], mapping: MessageProtoDescriptor[T]): Unit =
    object FieldName:
      def unapply(num: Int): Option[String] = 
        try Some(mapping.fieldName(num))
        catch 
          case _: MatchError => None
    var done: Boolean = false
    while !done do
      in.readTag match
        case 0 => done = true
        case Tag(wireType, num @ FieldName(field)) =>
          readField(struct.putField(field), mapping.fieldDesc(num), wireType)
