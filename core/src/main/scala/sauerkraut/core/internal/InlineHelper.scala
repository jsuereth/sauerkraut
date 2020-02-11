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