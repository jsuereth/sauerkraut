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

object InlineHelper
  /** Given an input type of string constants, pulls the string at a given index. */
  inline def summonLabel[T <: Tuple](idx: Int): String = 
    inline if(idx == 0) then
      inline compiletime.erasedValue[T] match
        case _: (c *: next) => compiletime.constValue[c].asInstanceOf[String]
        case _ => compiletime.error(s"Invalid index $idx into tuple")
    else
      inline compiletime.erasedValue[T] match 
        case _: (c *: next) => summonLabel[next](idx-1)
        case _ => compiletime.error(s"Invalid index $idx into tuple")
  /** Given an input type of string constants, return a runtime string list of the values. */
  inline def summonLabels[T <: Tuple]: List[String] =
    inline compiletime.erasedValue[T] match
      case _: (c *: next) => compiletime.constValue[c].asInstanceOf[String] :: summonLabels[next]
      case _ => Nil