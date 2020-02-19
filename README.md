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
