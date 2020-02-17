package sauerkraut.format.pb

  def hexString(buf: Array[Byte]): String =
    buf.map(b => f"$b%02x").mkString("")