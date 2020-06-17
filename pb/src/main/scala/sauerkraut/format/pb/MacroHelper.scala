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
import scala.tasty._
import scala.quoted._

class MacroHelper(val qctx: QuoteContext):
  import qctx.tasty.{_,given _}
  given QuoteContext = qctx
  /** 
   * Extracts the refinement + label bounds added
   * to a Mirror.Of type when it is a product.
   * 
   * Note: This is INCREDIBLY tied to the current impl.
   */
  object ProductOfRefinement:
      def unapply(t: Type): Option[(Type, Type)] =
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
    def unapply(t: Type): Option[(Type, Type)] =
      t match
        case AppliedType(TypeRef(_, "*:"),List(head : Type, cons: Type)) => 
          Some(head, cons)
        case _ => None // TODO

  def fieldNumberFromType(elem: Type): Int =
    elem match
      // TODO - Ensure this is a `@field(num)` annotation. 
      case AnnotatedType(tpe, Apply(term, List(Literal(Constant(num))))) =>
        num.asInstanceOf[Int]

  /** Creates a type + name array from tuple-types of ElemLabels + ElemTypes. */
  def typesAndNames(elems: Type, labels: Type): Map[String, Type] =
    (elems, labels) match
      case (TupleCons(elem, nextElems), TupleCons(ConstantString(label), nextLabels)) => 
        Map("test" -> elem)
        typesAndNames(nextElems, nextLabels) + (label -> elem)
      case _ => Map.empty

  /** Extracts the string from any constnat type.  This just toString's the value. */
  object ConstantString:
    def unapply(t: Type): Option[String] =
      t match
        case ConstantType(c) => Some(c.value.toString)
        case _ => None
  
  def fieldNamesTypesAndNumber(mirrorProductType: Type): Map[String, (Type, Int)] =
    mirrorProductType.widen match {
      case ProductOfRefinement(elems, labels) =>
        val fields = typesAndNames(elems, labels)
        fields.map {
          case (k,v) => (k, (v, fieldNumberFromType(v)))
        }
      case None => Map.empty
    }