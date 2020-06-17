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
package internal

object InlineHelper:
  /** Given an input type of string constants, pulls the string at a given index. */
  inline def summonLabel[T <: Tuple](idx: Int): String = 
    inline if(idx == 0) then
      inline compiletime.erasedValue[T] match
        case _: (c *: next) => summonString[c]
        case _ => compiletime.error(s"Invalid index $idx into tuple")
    else
      inline compiletime.erasedValue[T] match 
        case _: (c *: next) => summonLabel[next](idx-1)
        case _ => compiletime.error(s"Invalid index $idx into tuple")
  /** Given an input type of string constants, return a runtime string list of the values. */
  inline def summonLabels[T <: Tuple]: List[String] =
    // TODO - return an array?
    inline compiletime.erasedValue[T] match
      case _: (c *: next) => summonString[c] :: summonLabels[next]
      case _ => Nil
  /** Given a type that is a string constant, return a runtime literal string of that constant. */
  inline def summonString[Label]: String = compiletime.constValue[Label].asInstanceOf[String]
  /** Given a runtime label value, will write an if/else chain to determine index of the label. */
  inline def labelIndexLookup[T <: Tuple](label: String): Int =
    inline compiletime.erasedValue[T] match
      case _: (c *: next) => 
        if (summonString[c] == label) 0
        else 1 + labelIndexLookup[next](label)
      case _ => throw IndexOutOfBoundsException(s"Cannot find $label")

  /** Given a runtime index, will look over the compile-time constant strings and select one at that index. */
  inline def labelLookup[T <: Tuple](idx: Int): String =
    inline compiletime.erasedValue[T] match
      case _: (c *: next) =>
         if idx == 0
         then summonString[c]
         else labelLookup[next](idx-1)
      case _ => throw IndexOutOfBoundsException(s"Cannot find label at index: $idx")