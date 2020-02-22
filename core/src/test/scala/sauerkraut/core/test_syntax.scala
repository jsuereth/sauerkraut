package sauerkraut.core

// TODO - Maybe move this somewhere shared so we can re-use it
// in other places.

// e.g. this could be useful for format/evolution compliance tests.

// Helper method to deal with primitive writing.
def [T](b: Builder[?]) putPrimitive(value: T): Builder[T] =
  b match
    case pb: PrimitiveBuilder[_] => 
      pb.asInstanceOf[PrimitiveBuilder[T]].putPrimitive(value)
      pb.asInstanceOf[Builder[T]]
    case _ => 
      throw RuntimeException(s"$b is not an instance of PrimitiveBuilder[_]")

def [T](b: Builder[T]) putField(name: String): Builder[?] =
  b match
    case sb: StructureBuilder[T] => sb.putField(name)
    case _ => 
      throw RuntimeException(s"$b is not an instance of StructureBuilder[_]") 

def [T](b: Builder[T]) putElement(): Builder[?] =
  b match
    case cb: CollectionBuilder[_, T] => cb.putElement()
    case _ =>
      throw RuntimeException(s"$b is not an instance of CollectionBuilder[_]")

def [T](b: Builder[T]) putChoice(name: String): Builder[?] =
  b match
    case cb: ChoiceBuilder[T] => cb.putChoice(name)
    case _ =>
      throw RuntimeException(s"$b is not an instance of ChoiceBuilder[_]")
