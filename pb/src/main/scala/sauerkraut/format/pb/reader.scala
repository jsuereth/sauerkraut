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
    extends PickleReader
  def push[T](b: core.Builder[T]): core.Builder[T] = 
    b match
      case b: core.StructureBuilder[T] =>
        readStructure(b, repo.find(b.tag)) 
        b
      case _ => throw BuildException(s"Unable to deserialize proto to $b", null)

  def readStructure[T](struct: core.StructureBuilder[T], mapping: TypeDescriptorMapping[T]): Unit =
    object FieldName
      def unapply(num: Int): Option[String] = 
        try Some(mapping.fieldName(num))
        catch 
          case _: MatchError => None
    def readField(field: String): Unit =
      // TODO - unify field descriptor....
      mapping.fieldDescriptor(field) match
        case Some(fd) =>
          // Push a nested structure/enum type.
          // TODO - handle enums
          struct.putField(field) match
            case x: core.StructureBuilder[?] =>
              limitByWireType(WIRETYPE_LENGTH_DELIMITED) {
                readStructure(x, fd)
              }
            case y => throw BuildException(s"Cannot read structure $field in ${struct.tag} as $y", null)
        case None => 
          // Push a raw type.
          // TODO - does this belong in this class?
          struct.putField(field) match 
            case x: core.CollectionBuilder[_,_] => 
              RawBinaryPickleReader(in).push(x.putElement())
            case y: core.Builder[_] => RawBinaryPickleReader(in).push(y)
        
    var done: Boolean = false
    while !done do
      in.readTag match
        case 0 => done = true
        case Tag(wireType, FieldName(field)) =>
          readField(field)
  inline private def limitByWireType[A](wireType: Int)(f: => A): Unit =
    // TODO - if field is a STRING we do not limit by length.
    if wireType == WIRETYPE_LENGTH_DELIMITED
    then
      var length = in.readRawVarint32()
      val limit = in.pushLimit(length)
      f
      in.popLimit(limit)
    else f
