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
    // TODO - put ".tag" on Builder? 
    b match
      case b: core.StructureBuilder[T] =>
        pushWithDesc(b, repo.find(b.tag))
      case _ => throw BuildException(s"Unable to deserialize proto to $b", null)

  private def pushWithDesc[T](b: core.Builder[T], desc: ProtoTypeDescriptor[T]): core.Builder[T] =
    (b, desc) match
      case (b: core.StructureBuilder[T], s: MessageProtoDescriptor[_]) => readStructure(b, s)
      case (b: core.CollectionBuilder[_,_], s: CollectionTypeDescriptor[_,_]) => pushWithDesc(b.putElement(), s.element.asInstanceOf)
      case _ => throw BuildException(s"Unable to find proto descriptor for $b", null)
    b
      

  def readStructure[T](struct: core.StructureBuilder[T], mapping: MessageProtoDescriptor[T]): Unit =
    object FieldName:
      def unapply(num: Int): Option[String] = 
        try Some(mapping.fieldName(num))
        catch 
          case _: MatchError => None
    def readField(field: String, fieldNum: Int): Unit =
      struct.putField(field) match
        case choice: core.ChoiceBuilder[_] =>
          ???
        case struct: core.StructureBuilder[_] =>
          mapping.fieldDesc(fieldNum) match
            case msg: MessageProtoDescriptor[_] =>
              limitByWireType(WIRETYPE_LENGTH_DELIMITED) {
                readStructure(struct, msg)
              }
            case other => throw BuildException(s"Unable to find descriptor for ${struct.tag}, found $other", null)
        case col: core.CollectionBuilder[_,_] =>
          // TODO - this is WRONG, we need to lookup the descriptor of
          // the field.
          RawBinaryPickleReader(in).push(col.putElement())
        case prim: core.PrimitiveBuilder[_] =>
          RawBinaryPickleReader(in).push(prim)
        
    var done: Boolean = false
    while !done do
      in.readTag match
        case 0 => done = true
        case Tag(wireType, num @ FieldName(field)) =>
          readField(field, num)
  inline private def limitByWireType[A](wireType: Int)(f: => A): Unit =
    // TODO - if field is a STRING we do not limit by length.
    if wireType == WIRETYPE_LENGTH_DELIMITED
    then
      var length = in.readRawVarint32()
      val limit = in.pushLimit(length)
      f
      in.popLimit(limit)
    else f
