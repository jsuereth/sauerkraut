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
  /** Returns the result of all the values placed into this builder. */
  def result: T

// TODO:  `object NoBuilder` to use when a field does not exist for compatibility.


/** Represents a `builder` that can be used to generate a structure from a pickle. */
trait StructureBuilder[T] extends Builder[T]
  /** The tag used for this structure. */
  def tag: format.Struct[T]
  /** 
   * Puts a field into this builder.
   * 
   * Note: For a field that are collections, this may be called mulitple times
   * with individual elements.
   */
  def putField[F](name: String): Builder[F]

/** 
 * Represents a `builder` that can be used to generate one of a variety of instances
 * from a pickle. 
 */
trait ChoiceBuilder[T] extends Builder[T]
  /** The tag of the type being built by this builder. */
  def tag: format.Choice[T]
  /** Grabs the builder for a given choice and assigns the result of the current builder to that value. */
  def putChoice[F](name: String): Builder[F]

/** Represents a builder of collections from pickles. */
trait CollectionBuilder[E, To] extends Builder[To]
  /** Places an element into the collection.   Returns a new builder for the new element. */
  def putElement(): Builder[E]

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
  import internal.InlineHelper.{summonLabels,labelIndexLookup}
  /** Derives Builders for any product-like class. */
  inline def derived[T](given m: Mirror.Of[T]): Buildable[T] =
    new Buildable[T] {
        // TODO - use the FastTypeTag for this.
        private val knownFieldNames: Array[String] =
          summonLabels[m.MirroredElemLabels].toArray
        override def newBuilder: Builder[T] =
          inline m match
            case m: Mirror.ProductOf[T] =>
               productBuilder[T, m.type](m, knownFieldNames)
            case m: Mirror.SumOf[T] =>
              sumBuilder[T, m.type](m)
            case _ => compiletime.error("Cannot derive builder for non-struct classes")
    }
  /** Summons new builders for a tuple of types. */
  inline def buildersFor[Elems <: Tuple]: List[Builder[_]] =
    inline erasedValue[Elems] match
      case _: (h *: tail) => (builderFor[h] :: buildersFor[tail])
      case _: Unit => Nil
  /** Summons a single new builder for a type. */
  inline def builderFor[T]: Builder[T] =
    summonFrom {
      case b: Buildable[T] => b.newBuilder
      // TODO - this is terrible, figure out a way not to
      // waste so much compute for this.
      case m: Mirror.Of[T] => derived[T].newBuilder
      case _ => compiletime.error("Unable to construct builder")
    }
    
  inline def productBuilder[T, M <: Mirror.ProductOf[T]](m: M,
    fieldNames: Array[String]): Builder[T] = 
    new StructureBuilder[T] {
        override val tag: format.Struct[T] = format.fastTypeTag[T]().asInstanceOf
        override def toString(): String = s"Builder[${format.typeName[T]}]"
        private var fields = buildersFor[m.MirroredElemTypes]
        override def putField[F](name: String): Builder[F] =
          // TODO - this throws IndexOutOfBoundsException.  We should have better error message. 
          try
            val idx = labelIndexLookup[m.MirroredElemLabels](name)
            fields(idx).asInstanceOf[Builder[F]]
          catch
            case e: IndexOutOfBoundsException =>
              throw BuildException(s"Unable to find field $name", e)
        override def result: T =
          m.fromProduct(
              ArrayProduct(
                // TODO - this `map` is now one of the more signficant slowdowns from sauerkraut. 
                fields
                .map(b =>
                    b.asInstanceOf[Builder[_]].result.asInstanceOf[Object]
                ).toArray[Object])
          )
    }
  inline def sumBuilder[T, M <: Mirror.SumOf[T]](m: M): Builder[T] =
    new ChoiceBuilder[T] {
      private var choiceBuilder: Builder[T] = null
      // TODO - see if we can encode this as inline function against a pregenerated array of builders.
      private val builderLookup: Map[String, Builder[_]] =
        (summonLabels[m.MirroredElemLabels] zip
        buildersFor[m.MirroredElemTypes].toArray).toMap.asInstanceOf[Map[String, Builder[_]]]
      override val tag = format.fastTypeTag[T]().asInstanceOf[format.Choice[T]]
      // TODO - we should allow by-name *or* by-ordinal rather than reverse engineering that junk.
      override def putChoice[F](name: String): Builder[F] =
        choiceBuilder = builderLookup(name).asInstanceOf[Builder[T]] 
        choiceBuilder.asInstanceOf[Builder[F]]
      override def result: T = choiceBuilder.result
    }
