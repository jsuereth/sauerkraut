# Protocol Buffer Formats

This library makes use of `CodedInputStream`/`CodedOutputStream` from the Java protocol buffer library to
implement two formats for Scala:

1. A `RawBinary` format which looks similar to protocol buffers, but does not maintain all of its characteristics.
   Specifically this one:
   * Does not preserve field numberings across refactoring/shuffling of class definition.
   * Does not require a `message` protocol definition.
   * Should be able to handle any type allowed by sauerkraut.
2. A `Protos` format intended for bi-directional protocol buffer serialization.
   * Requires derivation of `ProtoTypeDescriptor`.
   * Allows `@field(num)` annotations to preserve field numbering on members of classes.
   * Is still HIGHLY experimental and incomplete.


# TODOs

- [ ] Enum support for descriptor-based serialization
- [ ] Collection support for descriptor-based serialization
- [ ] Optimisation for descriptor-based serialization
- [ ] ByteChannel input