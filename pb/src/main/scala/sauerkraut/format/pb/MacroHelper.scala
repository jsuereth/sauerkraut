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

package sauerkraut.format.pb

import deriving._
import scala.quoted._

class MacroHelper(using val qctx: Quotes):
  import quotes.reflect._
  /** 
   * Extracts the refinement + label bounds added
   * to a Mirror.Of type when it is a product.
   * 
   * Note: This is INCREDIBLY tied to the current impl.
   */
  object ProductOfRefinement:
    def unapply(t: TypeRepr): Option[(TypeRepr, TypeRepr)] =
      t.widen match
        case Refinement(
            Refinement(_,
              "MirroredElemTypes",
              TypeBounds(elems, _)),
            "MirroredElemLabels",
            TypeBounds(labels,_)) =>
          Some(elems, labels)
        case _ => None
  object TupleCons:
    def unapply(t: TypeRepr): Option[(TypeRepr, TypeRepr)] =
      t match
        case AppliedType(TypeRef(_, "*:"),List(head: TypeRepr, cons: TypeRepr)) => 
          Some(head, cons)
        case _ => None // TODO

  def fieldNumberFromType(elem: TypeRepr): Int =
    elem match
      // TODO - Ensure this is a `@field(num)` annotation. 
      case AnnotatedType(tpe, Apply(term, List(Literal(IntConstant(num))))) =>
        num.asInstanceOf[Int]

  /** Creates a type + name array from tuple-types of ElemLabels + ElemTypes. */
  def typesAndNames(elems: TypeRepr, labels: TypeRepr): Seq[(String, TypeRepr)] =
    (elems, labels) match
      case (TupleCons(elem, nextElems), TupleCons(ConstantString(label), nextLabels)) => 
        (label -> elem) +: typesAndNames(nextElems, nextLabels)
      case _ => Seq.empty

  /** Extracts the string from any constnat type.  This just toString's the value. */
  object ConstantString:
    def unapply(t: TypeRepr): Option[String] =
      t match
        case ConstantType(c) => Some(c.value.toString)
        case _ => None

  /** Extracts an (idemptotently sorted) list of field names, types and "numbers" (via proto annotations). */
  def fieldNamesTypesAndNumber(mirrorProductType: TypeRepr): Seq[(String, (TypeRepr, Int))] =
    mirrorProductType.widen match {
      case ProductOfRefinement(elems, labels) =>
        val fields = typesAndNames(elems, labels)
        fields.map {
          case (k,v) => (k, (v, fieldNumberFromType(v)))
        }
      case None => Seq.empty
    }