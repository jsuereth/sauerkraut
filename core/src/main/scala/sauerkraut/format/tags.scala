package sauerkraut.format

enum FastTypeTag[T]
  case UnitTag extends FastTypeTag[Unit]
  case BooleanTag extends FastTypeTag[Boolean]
  case CharTag extends FastTypeTag[Char]
  case ShortTag extends FastTypeTag[Short]
  case IntTag extends FastTypeTag[Int]
  case LongTag extends FastTypeTag[Long]
  case FloatTag extends FastTypeTag[Float]
  case DoubleTag extends FastTypeTag[Double]
  case StringTag extends FastTypeTag[String]
  // TODO - case ArrayByte extends FastTypeTag[Array[Byte]]
  // TODO - figure out if we want to keep a string-ified class name or not.
  case Erased[T]() extends FastTypeTag[T]


import compiletime.erasedValue
inline def fastTypeTag[T](): FastTypeTag[T] =
    inline erasedValue[T] match
        case _: Unit => FastTypeTag.UnitTag.asInstanceOf
        case _: Boolean => FastTypeTag.BooleanTag.asInstanceOf
        case _: Char => FastTypeTag.CharTag.asInstanceOf
        case _: Short => FastTypeTag.ShortTag.asInstanceOf
        case _: Int => FastTypeTag.IntTag.asInstanceOf
        case _: Long => FastTypeTag.LongTag.asInstanceOf
        case _: Float => FastTypeTag.FloatTag.asInstanceOf
        case _: Double => FastTypeTag.DoubleTag.asInstanceOf
        case _: String => FastTypeTag.StringTag.asInstanceOf
        case _ => FastTypeTag.Erased[T]()
    