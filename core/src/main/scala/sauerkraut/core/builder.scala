package sauerkraut
package core

import format.FastTypeTag

/** 
 * Represents something that we can construct a builder for. 
 */
trait Buildable[T]
  /** Construct a new builder that can generate type T. */
  def newBuilder: Builder[T]

/** 
 * Represents something that can build type T out of a pickle.
 * 
 * This is a push API, where read values are pushed into the builder, constructing the type.
 */
sealed trait Builder[T]
  def result: T

/** Represents a `builder` that can be used to generate a structure from a pickle. */
trait StructureBuilder[T] extends Builder[T]
  /** The known field names for this structure. */
  def knownFieldNames: List[String]
  /** 
   * Puts a field into this builder.
   * 
   * Note: For a field that are collections, this may be called mulitple times
   * with individual elements.
   */
  def putField[F](name: String): Builder[F]
  /** Returns the resulting built structure after pushing in all pieces of data. */
  def result: T

/** Represents a builder of collections from pickles. */
trait CollectionBuilder[E, To] extends Builder[To]
  /** Places an element into the collection.   Returns a new builder for the new element. */
  def putElement(): Builder[E]
  /** Returns the built collection of elements. */
  def result: To

/** A builder for primitives.  basically just writes values into their final location. */
trait PrimitiveBuilder[P] extends Builder[P]
  /** The tag of the primitive.  Determines how a pickle is read. */
  def tag: format.PrimitiveTag[P]
  /** Places the primitive into the builder. */
  def putPrimitive(value: P): Unit


// Now we attempt to derive a builder.
object Buildable
  import deriving._
  import scala.compiletime.{erasedValue,constValue,summonFrom}
  import internal.InlineHelper.summonLabels
  /** Derives Builders for any product-like class. */
  inline def derived[T](given m: Mirror.Of[T]): Buildable[T] =
    new Buildable[T] {
        override def newBuilder: Builder[T] =
          inline m match
            case m: Mirror.ProductOf[T] =>
               productBuilder[T, m.type](m)
            case _ => compiletime.error("Cannot derive builder for non-struct classes")
    }
  /** Summons new builders for a tuple of types. */
  inline def buildersFor[Elems <: Tuple]: Tuple.Map[Elems,Builder] =
    inline erasedValue[Elems] match
      case _: (h *: tail) => (builderFor[h] *: buildersFor[tail]).asInstanceOf[Tuple.Map[Elems, Builder]]
      case _: Unit => ().asInstanceOf[Tuple.Map[Elems, Builder]]
  /** Summons a single new builder for a type. */
  inline def builderFor[T]: Builder[T] =
    summonFrom {
      case b: Buildable[T] => b.newBuilder
      case _ => compiletime.error("Unable to construct builder")
    }

  // Ugly type for tuple match.  
  type BuiltValue[T] = T match
    case Builder[t] => t
    case Unit => Unit
    
  inline def productBuilder[T, M <: Mirror.ProductOf[T]](m: M): Builder[T] = 
    new StructureBuilder[T] {
        override def toString(): String = s"Builder[${format.typeName[T]}]"
        private var fields = buildersFor[m.MirroredElemTypes]
        override def knownFieldNames: List[String] =
          summonLabels[m.MirroredElemLabels]
        // TODO - lookup tuple by index?
        override def putField[F](name: String): Builder[F] =
          // TODO - FIX THIS TO NOT BE DYNAMIC LOOKUP, something more like
          // a pattern match.
          // ALSO fix the error messages if builders are not ready....
          fields.toArray(knownFieldNames.indexOf(name)).asInstanceOf[Builder[F]]
        override def result: T =
          m.fromProduct(
              fields
                .map[BuiltValue]([builder] => 
                  (b: builder) =>
                    b.asInstanceOf[Builder[_]]
                     .result
                     .asInstanceOf[BuiltValue[builder]]
                ).asInstanceOf[Product]
          )
    }
  