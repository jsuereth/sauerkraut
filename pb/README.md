# Protocol Buffer Formats

This library provides a "protocol buffer" like format that aims to be as compatible as possible
with Google's protocol buffer library.   This isn't always possible due to types of encoding possible
in Scala that are not in protocol buffers.

**Note: this library is highly experimental and incomplete**


Example:

```scala
import sauerkraut.{pickle,write,read, Field}
import sauerkraut.core.{Writer, Buildable, given}
import sauerkraut.format.pb.{Proto,,given}


case class MyMessageData(value: Int @Field(3), someStuff: Array[String] @Field(2))
    derives Writer, Buildable

def write(out: java.io.OutputStream): Unit = 
  pickle(Proto).to(out).write(MyMessageData(1214, Array("this", "is", "a", "test")))
```

This example serializes to the equivalent of the following protocol buffer message:

```proto
message MyMessageData {
  int32 value = 3;
  repeated string someStuff = 2;
}
```

# TODOs

- [ ] Document Field selection for case classes
- [ ] Document encoding of "Choice" into proto
- [ ] Define how "raw" (non-case-class) outer-serialization is performed
- [ ] Define un-encodable protobuf concepts
- [ ] Enum support for descriptor-based serialization
- [X] Collection support for descriptor-based serialization
- [X] Optimisation for descriptor-based serialization
- [ ] ByteChannel input