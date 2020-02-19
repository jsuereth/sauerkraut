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

Or, if you wanted something in binary:

```scala
import format.pb.{RawBinary,given}
import sauerkraut.{pickle,read,write}

val out = new java.io.ByteArrayOutputStream()
pickle(RawBinary).to(out).write(MyMessage("test", 1))

val in = java.io.ByteArrayInputStream(out.toByteArray)
val msg = pickle(RawBinary).from(in).read[MyMessage]
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


# Current Formats

## Json
- [X] Jawn Based Reader
- [X] Simple Writer

## Binary
- [X] Proto-like Reader
- [X] Proto-like Writer

## Protocol Buffers
- [X] Compatible Writer
- [ ] Compatible Reader

## NBT
- [ ] Reader
- [ ] Writer
