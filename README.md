# Sauerkraut

The library for those cabbage lovers out there who want
to send data over the wire.

A revitalization of Pickling in the Scala 3 world.

## Usage

When defining over-the-wire messages, do this:

```scala
import sauerkraut.core.{Reader,Writer,given}
case class MyMessage(field: String, data: Int)
  derives Reader, Writer
```

Then, when you need to serialize, pick a format and go:

```scala
import format.json.{Json,given}
import sauerkraut.pickle

val out = StringWriter()
pickle(Json).to(out).write(MyMessage("test", 1))
println(out.toString())
```

Or, if you wanted something binary looking

```scala
import format.pb.{RawBinary,given}
import sauerkraut.pickle

val out: java.io.OutputString =
   new java.io.ByteArrayOutputStream()
pickle(RawBinary).to(out).write(MyMessage("test", 1))
```