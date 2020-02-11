package sauerkraut
package core


/** A reader from some format of objects of the type T. */
trait Reader[T]
  // TODO - do we need a `tag` to disambiguate the thing we're reading?
  def read(pickle: format.PickleReader): T

object Reader
  import deriving._
  import scala.compiletime.{erasedValue,constValue,summonFrom}
  import internal.InlineHelper.summonLabel
  /** Derives a Reader for Product/Sum types using readers available on given scope. */
  inline def derived[T](given m: Mirror.Of[T]): Reader[T] =
    new Reader[T] {
      def read(pickle: format.PickleReader): T =
        inline m match
          case m: Mirror.ProductOf[T] =>
            // TODO - asssert the tag matches this type
            pickle.beginEntry()
            val value = m.fromProduct(readFields[m.MirroredElemTypes, m.MirroredElemLabels](pickle, 0).asInstanceOf).asInstanceOf[T]
            pickle.endEntry()
            value
          case _ => compiletime.error("Cannot derive serialization for non-product structures.")
    }

  inline private def readFields[Fields <: Tuple, Labels <: Tuple](pickle: format.PickleReader, idx: Int): Fields =
    inline erasedValue[Fields] match {
      case _: (elem *: elems) => 
        (readField[elem](pickle, summonLabel[Labels](idx)) *: readFields[elems, Labels](pickle, idx+1)).asInstanceOf[Fields]
      case _: Unit=> ().asInstanceOf[Fields]
    }

  inline private def readField[T](
    pickle: format.PickleReader,
    fieldName: String
  ): T =
    summonFrom {
      case reader: Reader[T] => reader.read(pickle.readField(fieldName))
    }