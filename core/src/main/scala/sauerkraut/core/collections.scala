package sauerkraut
package core

import format.PickleWriter

// TODO - make generic for all collections.
class CollectionWriter[T: Writer]() extends Writer[Iterable[T]]
  override def write(value: Iterable[T], pickle: PickleWriter): Unit =
    val c = pickle.beginCollection(value.size)
    for 
      item <- value
    do
      c.putElement(itemWriter => summon[Writer[T]].write(item, itemWriter))
    c.endCollection()

given [T](using Writer[T]) as Writer[List[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Seq[T]] = CollectionWriter[T]().asInstanceOf
given [T](using Writer[T]) as Writer[Iterable[T]] = CollectionWriter[T]().asInstanceOf
