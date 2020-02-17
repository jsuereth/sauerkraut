package sauerkraut
package format
package pb

import com.google.protobuf.{CodedInputStream}
import collection.mutable.Builder

class DescriptorBasedProtoReader(in: CodedInputStream) extends PickleReader
  def readPrimitive[T](tag: PrimitiveTag[T]): T = ???
  def readStructure[T](work: StructureReader => T): T = ???
  def readCollection[E, To](builder: Builder[E, To],
      elementReader: PickleReader => E): To = ???

class DescriptorBasedProtoStructureReader(
    in: CodedInputStream,
    mapping: MessageTypeDescriptor[?])
    extends StructureReader
  def readField[T](name: String, fieldReader: PickleReader => T): T = ???

