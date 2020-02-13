package sauerkraut.format

/**
 * A writer of pickled content.  This is a mutable API, intended to be called in certain specific ways.
 *
 * Here are a few static rules that all picklers must follow when using this interface.
 *
 * 1. There will be one endEntry() for every beginEntry() call.
 * 2. There will be one endCollection() for every beginCollection() call.
 * 3. Every beginCollection()/endCollection() pair will be inside a beginEntry()/endEntry() pair.
 * 4. Every putElement() call must happen within a beginCollection()/endCollection() block.
 * 5. Every putField() call must happen within a beginEntry()/endEntry() block.
 * 6. There is no guarantee that putElement() will be called within a beginCollectoin()/endCollection() pair.
 *    i.e. we can write empty collections.
 * 7. There is no guarantee that putField will be called within a beginEntry()/endEntry() pair.
 *    i.e. if we don't put any fields, this means the entry was for a "primitive" type, at least what
 *    The pickling library considers primitives.
 * 8. The order of putField calls in any pickler will be the exact same ordering when unpickling, if the format
 *    is compatible.
 *
 * Here is a list of all types the auto-generated Picklers considers "primitives" and must be directly supported by
 * any PickleWriter:
 *
 *   - Nothing
 *   - Null
 *   - Unit
 *   - Byte
 *   - Char
 *   - String
 *   - Short
 *   - Int
 *   - Long
 *   - Float
 *   - Double
 *   - Ref  (for circular object graphs)
 *   - ArrayByte
 *   - ArrayShort
 *   - ArrayChar
 *   - ArrayInt
 *   - ArrayLong
 *   - ArrayBoolean
 *   - ArrayFloat
 *   - ArrayDouble
 */
trait PickleWriter
  /** Called to denote that an structure is about to be serialized.
    * @param picklee
    *                The structure to be serialized.
    * @param tag
    *                The tag to use when pickling this entry.   Tags must be serialized/restored, unless
    *                otherwise hinted that it can be elided.
    * @return
    *                A pbuilder instance a pickler can use to serialize the picklee, if it's a complex type.
    */
  def beginStructure(picklee: Any, tag: FastTypeTag[_]): PickleStructureWriter

  /** Writes a primitive into the pickle. */
  def putPrimitive(picklee: Any, tag: FastTypeTag[_]): Unit
  /**
   * Denotes that a collection of elements is about to be pickled.
   *
   * Note: This must be called after beginEntry()
   * @param length   The length of the collection being serialized.
   * @return  A pickler which can serialzie the collection.
   */
  def beginCollection(length: Int): PickleCollectionWriter
  /** Flush any pending writes down this writer. */
  def flush(): Unit

/** A mechanism to write a 'structure' to the pickle. 
 *  Structures are key-value pairs of 'fields'.
 */
trait PickleStructureWriter
  /**
   * Serialize a "field" in a complex structure/object being pickled.
   * @param name  The name of the field to serialize.
   * @param pickler  A callback which will be passed an appropriate pickler.
   *                 You should ensure this function will perform a beginEntry()/endEntry() block.
   * @return A builder for remaining items in the current complex structure being pickled.
   */
  def putField(name: String, pickler: PickleWriter => Unit): PickleStructureWriter

  /**
   * Call this to denote that the given primitive, collection or structure being pickled is completed.
   */
  def endStructure(): Unit  

/** A writer of collection elements. */
trait PickleCollectionWriter
   /**
   * Places the next element in the serialized collection.
   *
   * Note: This must be called after beginCollection().
   * @param pickler  A callback which is passed a pickler able to serialize the item in the collection.
   * @return  A pickler which can serialize the next element of the collection.
   */
  def putElement(pickler: PickleWriter => Unit): PickleCollectionWriter
  /** Denote that we are done serializing the collection. */
  def endCollection(): Unit