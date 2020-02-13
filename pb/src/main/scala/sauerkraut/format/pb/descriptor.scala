package sauerkraut
package format
package pb

/** 
 * A descriptor for how a Scala case class/enum matches
 * a protocol buffer definitiion.
 * 
 * This is used to serialize the class when available.
 */
trait TypeDescriptorMapping[T]
  def fieldNumber(name: String): Int
  def fieldDescriptor[F](name: String): Option[TypeDescriptorMapping[F]]

/**
 * A repository for type descriptors mappings that will be used
 * in serialization/deserialization.
 */ 
trait TypeDescriptorRepository
  def find[T](tag: FastTypeTag[T]): TypeDescriptorMapping[T]