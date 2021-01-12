package sauerkraut.format.pb.streams

/** Wire format tags + flags for Protocol buffer encoding. */
enum WireFormat(val flag: Int):
  case VarInt extends WireFormat(0)
  case Fixed32 extends WireFormat(5)
  case Fixed64 extends WireFormat(1)
  case LengthDelimited extends WireFormat(2)
  // Unsupported:
  // - StartGroup, EndGroup

  // Constructs a proto tag for the wireformat type and the field number.
  def makeTag(fieldNumber: Int): Int = (fieldNumber << 3) | flag

object WireFormat:
  def extractFormat(tag: Int): Int = (tag & 7)
  def extractField(tag: Int): Int = (tag >>> 3)