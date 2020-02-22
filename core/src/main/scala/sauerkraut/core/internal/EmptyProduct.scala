package sauerkraut.core.internal

object EmptyProduct extends Product
  override def canEqual(other: Any): Boolean =
    other == this
  override def productArity: Int = 0
  override def productElement(n: Int): Any =
    throw RuntimeException("Cannot pull value from empty product.")

final class GenericProduct(values: Array[Object]) extends Product
  override def canEqual(other: Any): Boolean =
    other.isInstanceOf[GenericProduct]
  override def productArity: Int = values.length
  override def productElement(n: Int): Any = values(n)
