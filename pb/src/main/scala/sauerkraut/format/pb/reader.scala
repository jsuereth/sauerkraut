package sauerkraut
package format
package pb

import com.google.protobuf.{CodedInputStream}
import collection.mutable.Builder

class DescriptorBasedProtoReader(in: CodedInputStream)
    extends PickleReader
  def push[T](b: core.Builder[T]): core.Builder[T] = 
    b