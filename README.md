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

| Format | Reader | Writer | All Types | Evolution Friendly | Notes                                    |
| ------ | ------ | ------ | --------- | ------------------ | ---------------------------------------- |
| Json   | Yes    | Yes    | Yes       |                    | Uses Jawn for parsing                    |
| Binary | Yes    | Yes    | Yes       |                    |                                          |
| Protos | TBD    | Yes    | No        |                    | For bi-directional Protocol Buffer usage |
| NBT    | Yes    | Yes    | Yes       |                    |                                          |
| XML    | TBD    | TBD    | TBD       |                    |                                          |
| Pretty | No     | Yes    | No        |                    | For pretty-printing strings              |

## Json
Everyone's favorite non-YAML web data transfer format!   This uses Jawn under the covers for parsing, but
can write Json without any dependencies.


Example:
```scala
import sauerkraut.{pickle,read,write}
import sauerkraut.core.{Buildable,Writer, given}
import sauerkraut.format.json.Json

case class MyWebData(value: Int, someStuff: Array[String])
    derives Buildable, Writer

def read(in: java.io.InputStream): MyWebData =
  pickle(Json).from(in).read[MyWebData]
def write(out: java.io.OutputStream): Unit = 
  pickle(Json).to(out).write(MyWebData(1214, Array("this", "is", "a", "test")))
```

sbt build:
```scala
libraryDependencies += "com.jsuereth.sauerkraut" %% "json" % "<version>"
```


## Binary
A binary format loosely based on Protocol-Buffers.   Unlike protocol-buffers, this format can serialize any 
Scala type.

Example:
```scala
import sauerkraut.{pickle,read,write}
import sauerkraut.core.{Buildable,Writer, given}
import sauerkraut.format.pb.RawBinary

case class MyFileData(value: Int, someStuff: Array[String])
    derives Buildable, Writer

def read(in: java.io.InputStream): MyFileData =
  pickle(RawBinary).from(in).read[MyFileData]
def write(out: java.io.OutputStream): Unit = 
  pickle(RawBinary).to(out).write(MyFileData(1214, Array("this", "is", "a", "test")))
```

sbt build:
```
libraryDependencies += "com.jsuereth.sauerkraut" %% "pb" % "<version>"
```

## Protos
A new encoding for protocol buffers within Scala!  This supports a subset of all possible protocol buffer messages
but allows full definition of the message format within your Scala code.

Example:
```scala
import sauerkraut.{pickle,write}
import sauerkraut.core.{Writer, given}
import sauerkraut.format.pb.{Protos,TypeDescriptorMapping,field,given}


case class MyMessageData(value: Int @field(3), someStuff: Array[String] @field(2))
    derives TypeDescriptorMapping, Writer

val MyProtos = Protos[MyMessageData *: Unit]()

def write(out: java.io.OutputStream): Unit = 
  pickle(MyProtos).to(out).write(MyMessageData(1214, Array("this", "is", "a", "test")))
```

This example serializes to the equivalent of the following protocol buffer message:

```proto
message MyMessageData {
  int32 value = 3;
  repeated string someStuff = 2;
}
```


sbt build:
```scala
libraryDependencies += "com.jsuereth.sauerkraut" %% "pb" % "<version>"
```



# NBT
Named-Binary-Tags, a format popularized by Minecraft.

Example:
```scala
import sauerkraut.{pickle,read,write}
import sauerkraut.core.{Buildable,Writer, given}
import sauerkraut.format.nbt.Nbt

case class MyGameData(value: Int, someStuff: Array[String])
    derives Buildable, Writer

def read(in: java.io.InputStream): MyGameData =
  pickle(Nbt).from(in).read[MyGameData]
def write(out: java.io.OutputStream): Unit = 
  pickle(Nbt).to(out).write(MyGameData(1214, Array("this", "is", "a", "test")))
```

sbt build:
```scala
libraryDependencies += "com.jsuereth.sauerkraut" %% "nbt" % "<version>"
```

# XML
Everyone's favorite markup language for data transfer!

TODO - Using

# Pretty
A format that is solely used to pretty-print object contents to strings.  This does not have
a [PickleReader] only a [PickleWriter].

Example:
```scala
import sauerkraut._, sauerkraut.core.{Writer,given}
case class MyAwesomeData(theBest: Int, theCoolest: String) derives Writer

scala> MyAwesomeData(1, "The Greatest").prettyPrint
val res0: String = Struct(rs$line$2.MyAwesomeData) {
  theBest: 1
  theCoolest: The Greatest
}
```


# Design

We split Serialization into three layers:

1. The `source` layer.  It is expected these are some kind of stream.
2. The `Format` layer.  This is responsible for reading a raw source and converting into
   the component types used in the `Shape` layer.  See `PickleReader` and `PickleWriter`.
3. The `Shape` layer.  This is responsible for taking Primitives, Structs and Collections and
   turning them into component types.

It's the circle of data:
```
   Source   =>     format    =>  shape => memory =>  shape  =>   format    =>   Destination        

[PickleData] => PickleReader => Builder[T] => T => Writer[T] => PickleWriter => [PickleData]
```

This, hopefully, means we can reuse a lot of logic betwen various formats with light loss to efficiency.

*Note:  This library is not measuring performance yet.*

### Shape layer
The Shape layer is responsible for extracting Scala types into known shapes that can be used for
serialization.  These shapes, current, are `Collection`, `Structure` and `Primitive`.   Custom
shapes can be created in terms of these three shapes.

The Shape layer defines these three classes:
- `sauerkraut.core.Writer[T]`:
  Can translate a value into write* calls of Primitive, Structure or Collection.
- `sauerkraut.core.Builder[T]`:  
  Can accept an incomiing stream of collections/structures/primitives and build a value of T from them.
- `sauerkraut.core.Buildable[T]`:
  Can provide a `Builder[T]` when asked.

### Format layer
The format layer is responsible for mapping sauerkraut shapes (`Collection`, `Structure`, `Primitive`) into
the underlying format.  Not all shapes in sauerkraut will map exactly to underlying formats, and so each
format may need to adjust/tweak incoming data as appropriate.

The format layer has these primary classes:

- `sauerkraut.format.PickleReader`:  Can load data and push it into a Builder of type T
- `sauerkraut.format.PickleWriter`:  Accepts pushed structures/collections/primitives and places it into a Pickle

### Source Layer
The `source` layer is allowed to be any type that a format wishes to support.   Inputs and outputs are
provided to the API via these two classes:

- `sauerkraut.format.PickleReaderSupport[Input, Format]`:
  A given of this instance will allow the `PickleReader` to be constructed from a type of input.
- `sauerkraut.format.PickleWriterSupport[Output,Format]`:
  A given of this instance will allow `PickleWriter` to be constructed from a type of output.

This layer is designed to support any type of input and output, not just an in-memory store (like a Json Ast) or
a streaming input.  Formats can define what types of input/output (or execution environment) they allow.

## Writing a new format.

New formats are expected to provide the "format" + "source" layer implementations they require.

TODO - a bit more here.


## Core Concepts TODO list
- [X] Builder/Writer
  - [X] Primitive Types
  - [X] Collections
  - [X] Manually written builders/writers.
  - [X] Derived for Product Types
  - [ ] Derived for Sum Types

# Differences from Scala Pickling

There are a few major differences from the old [scala pickling project](http://github.com/scala/pickling).

- The core library is built for 100% static code generation.   While we think that dynamic (i.e. runtime-reflection-based)
  pickling could be built using this library, it is a non-goal.
  - Users are expected to rely on typeclass derivation to generate Reader/Writers, rather than using macros
  - The supported types that can be pickled are limited to the same supported by typeclass derivation or that
    can have hand-written `Writer[_]`/`Builder[_]` instances.
- Readers are no longer driven by the Scala type.  Instead we use a new `Buildable[A]`/`Builder[A}` design
  to allow each `PickleReader` to push value into a `Builder[A]` that will then construct the scala class.
- There have been no runtime performance optimisations around codegen.   Those will come as we test the
  limits of Scala 3 / Dotty.
- Format implementations are separate libraries.
- The `PickleWriter` contract has been split into several types to avoid misuse.  This places a heavier amount
  of lambdas in play, but may be offsite with optimisations in modern versions of Scala/JVM.
- The name is more German.


# Benchmarking

TODO - we should get a good set of these.

# Thanks

Thanks to everyone who contributed to the original pickling library for inspiration, with a few callouts.

- Heather Miller + Phillip Haller for the original idea, innovation and motivation for Scala.
- Havoc Pennington + Eugene Yokota for helping define what's important when pickling a protocol and evolving that protocol.