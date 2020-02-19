# Sauerkraut

The library for those cabbage lovers out there who want
to send data over the wire.

A revitalization of Pickling in the Scala 3 world.

## Usage

When defining over-the-wire messages, do this:

```scala
import sauerkraut.core.{Buildable,Writer,given}
case class MyMessage(field: String, data: Int)
  derives Buildable, Writer
```

Then, when you need to serialize, pick a format and go:

```scala
import format.json.{Json,given}
import sauerkraut.{pickle,read,write}

val out = StringWriter()
pickle(Json).to(out).write(MyMessage("test", 1))
println(out.toString())

val msg = pickle(Json).from(out.toString()).read[MyMessage]
```

# Current Formats

Here's a feature matrix for each format:

| Format | Reader     | Writer | All Types | Evolution Friendly | Notes                                    |
| ------ | ---------- | ------ | --------- | ------------------ | ---------------------------------------- |
| Json   | Yes (jawn) | Yes    | Yes       |                    |                                          |
| Binary | Yes        | Yes    | Yes       |                    |                                          |
| Protos | TBD        | Yes    | No        |                    | For bi-directional Protocol Buffer usage |
| NBT    | Yes        | Yes    | Yes       |                    |                                          |
| XML    | TBD        | TBD    | TBD       |                    |                                          |
| Pretty | No         | TBD    | No        |                    | For pretty-printing strings              |

## Json
Everyone's favorite non-YAML web data transfer format!   This uses Jawn under the covers for parsing, but
can write Json without any dependencies.
TODO - Using

## Binary
A binary format loosely based on Protocol-Buffers.   Unlike protocol-buffers, this format can serialize any 
Scala type.

TODO - Using

## Protos
A new encoding for protocol buffers within Scala!  This supports a subset of all possible protocol buffer messages
but allows full definition of the message format within your Scala code.

TODO - Using

# NBT
Named-Binary-Tags, a format popularized by Minecraft.

TODO - Using

# XML
Everyone's favorite markup language for data transfer!

TODO - Using

# Pretty
A format that is solely used to pretty-print object contents to strings.  This does not have
a [PickleReader] only a [PickleWriter].

TODO - Using


# Design

We split Serialization into three layers:

1. The `source` layer.  It is expected these are some kind of stream.
2. The `Format` layer.  This is responsible for reading a raw source and converting into
   the component types used in the `Shape` layer.  See `PickleReader` and `PickleWriter`.
3. The `Shape` layer.  This is responsible for taking Primitives, Structs and Collections and
   turning them into component types.

It's the circle of data:
```
< Source >        <format>        <shape> <memory> <shape>      <format>        < Destination >        

[PickleData] => PickleReader => Builder[T] => T => Writer[T] => PickleWriter => [PickleData]
```

Core:
- Writer[T]:  Can translate a value into write* calls of Primitive, Structure or Collection.
- Builder[T]:  Accepts values and places them into a Builder for type T.  Can report information used
               to drive pickler format.

Formats:
- PickleReader:  Can load data and push it into a Builder of type T
- PickleWriter:  Accepts pushed structures/collections/primitives and places it into a Pickle


# Pickling Core Concepts
A list of concepts within Scala types that must be supported in the pickler library.

- [X] Builder/Writer
  - [X] Primitive Types
  - [X] Collections
  - [X] Manually written builders/writers.
  - [X] Derived for Product Types
  - [ ] Derived for Sum Types
