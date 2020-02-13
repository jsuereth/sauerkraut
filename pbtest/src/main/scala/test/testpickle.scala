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