package sauerkraut
package core


/** A Writer from some format of objects of the type T. */
trait Writer[T]
  def write(value: T, pickle: format.PickleWriter): Unit

object Writer
  import scala.compiletime.{erasedValue,summonFrom}
  import deriving._
  import internal.InlineHelper.summonLabel
  /** Derives writers of type T. */
  inline def derived[T](given m: Mirror.Of[T]): Writer[T] =
    new Writer[T] {
      def write(value: T, pickle: format.PickleWriter): Unit =
        inline m match
          case m: Mirror.ProductOf[T] =>
            val writer = pickle.beginStructure(this, format.fastTypeTag[T]())
            writeElems[m.MirroredElemTypes, m.MirroredElemLabels](writer, value, 0)
            writer.endStructure()
          case _ => compiletime.error("Cannot derive serialization for non-product classes")
    }
  /** Writes all the fields (in Elems) to the structure writer. */
  inline private def writeElems[Elems <: Tuple, Labels <: Tuple](
    pickle: format.PickleStructureWriter, value: Any, idx: Int): Unit =
      inline erasedValue[Elems] match
        case _: (elem *: elems1) =>
          pickle.putField(summonLabel[Labels](idx),
            fieldPickle =>
              writeInl[elem](productElement[elem](value, idx), fieldPickle))
          writeElems[elems1, Labels](pickle, value, idx+1)
        case _: Unit => ()
      
  /** Write a particular value to a pickle ready for it. Looks up given Writer. */
  inline private def writeInl[A](value: A, pickle: format.PickleWriter): Unit =
    summonFrom {
      case writer: Writer[A] => writer.write(value, pickle)
    }