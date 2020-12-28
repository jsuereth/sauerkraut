# Latest Profiling

Here are the latest result runs we have (saved) so we
can start working on optimising components.  We do NOT
want our overhead to be more the 10% of 'raw' frameworks
of the same format.

Our goal is to:

* Beat the performance of any framework relying on
  runtime reflection
* Be within 10% performance of any framework that does
  code generation specifically for Scala.
* Match the performance of any framework that does
  code generation for Java *and* conventional/common
  usage does not match Java's usage.

## Full Results (2020-03-05)

*Test #1 - Nested messages + primitive collections*

| Framework  | Format    | ByteSize | Read (ns / op)   | Write ( ns / op) |
| ---------  | --------- | -------- | ---------------- | ---------------- |
| Sauerkraut | proto     | 165      | 1938.84          | 2384.86          |
| Sauerkraut | raw       | 197      | 3081.16          | 1642.89          |
| Sauerkraut | nbt       | 294      | 2321.81          | 1523.85          |
| Sauerkraut | json      | 235      | 3851.68          | 3858.37          |
| Sauerkraut | xml       | 1285     | 46972.45         | 8373.64          |
| Java Ser   |           | 1024     | 24490.29         | 6742.91          |
| Kryo       |           | 275      | 1748.03          | 1465.01          |
| JavaProto  |           | 161      | 380.43           | 522.19           |

Currently there's a bit too much overhead in Sauerkraut vs. Java's protocol
buffers. It still beats Java Serialization on all counts, but that's
nothing impressive in today's world.

Below is a journal of investigation and experiments in trying to improve these
numbers from the naive implementation into something ready-for-production.

## Areas to investigate:

- [X] Figure out where immutable.List.length is being called from (22.8%)
  - `Field.unapply` in RawBinaryReader was the issue.
  - We were doing validation that tags read MATCH what we see in
    the type.  May be more efficient just to let it throw and catch
    later.   We'll figure out more when we update error reporting.
- [X] Figure out where `StrictOptimisedLinearSeqOps.drop` is being called.
- [X] Split apart `RawBinaryPickleReader.push` (7.4%)
- [ ] Attempt to remove the crazy amount of boxing/unboxing we have w/ Primitives (2.5%)
  - [ ] Erase PrimitiveBuilders in codegen of `Buildable.derived[T]`
  - [ ] Add direct un-boxed signatures for putPrimitive.
- [ ] Figure out if `Mirror.ProductOf[T]#fromProduct` could be more efficient. (2.9%)
- [X] Support compressed repeated fields in PB format
- [ ] Add size-hint to `CollectionBuilder`

## New Stack Tracing Results (2020-12-24)

Writing pb format
```
[info]   7.8%  15.6% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.bufferUInt64NoTag
[info]   6.8%  13.7% com.google.protobuf.Utf8.encode
[info]   6.0%  11.9% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.bufferUInt32NoTag
[info]   4.1%   8.3% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.bufferInt32NoTag
[info]   3.8%   7.7% scala.collection.mutable.Growable.addAll
[info]   2.8%   5.7% scala.collection.AbstractIterable.<init>
[info]   2.3%   4.7% sauerkraut.format.pb.format$package$given_PickleWriterSupport_O_P.writerFor
[info]   2.0%   3.9% scala.collection.immutable.ArraySeq.copyToArray
[info]   2.0%   3.9% sauerkraut.benchmarks.SimpleMessage$$anon$1.write
[info]   1.5%   2.9% sauerkraut.benchmarks.WriteBenchmarks.setUp$$anonfun$1
[info]  10.9%  21.8% <other>
```

Reading pb format
```
[info]  10.4%  20.8% scala.collection.immutable.AbstractSeq.<init>
[info]   4.4%   8.8% sauerkraut.format.pb.DescriptorBasedProtoReader.pushWithDesc
[info]   3.1%   6.2% sauerkraut.format.pb.DescriptorBasedProtoReader.readStructure
[info]   2.7%   5.4% com.google.protobuf.CodedOutputStream.newInstance
[info]   2.6%   5.1% scala.collection.mutable.Growable.addAll
[info]   2.1%   4.2% sauerkraut.core.PrimitiveWriter.write
[info]   1.8%   3.6% com.google.protobuf.Utf8.encode
[info]   1.7%   3.4% sauerkraut.benchmarks.SimpleMessage$$anon$6$$anon$1.<init>
[info]   1.7%   3.4% scala.collection.AbstractIterable.<init>
[info]   1.4%   2.9% sauerkraut.format.pb.DescriptorBasedProtoReader.readField$1
[info]  18.2%  36.4% <other>
```

Here we notice a few issues:
- Collection Handling still needs work (`Growable.addAll` usage, `Abstract{Seq|Iterable}.<init>`)
- When writing, we are suffering from encoding into an array buffer, then using its resultisng size instead of precomputing size.
- `pushWithDesc` is contributing a significant amount to performance.  We should attempt to
  optimise this further if we can.

## Stack Tracing Results (2020-12-22)

We did a bunch of changes to support nested proto serialization w/ descriptors.  Here we investigate the stack trace for those directly, as the performance has dropped below that of 'raw', which should not be the case if we've done a good job.

sauerkraut pb format
```
## Complex
[info] ....[Thread state: RUNNABLE]........................................................................
[info]  10.2%  20.5% scala.collection.immutable.AbstractSeq.<init>
[info]   3.9%   7.9% sauerkraut.benchmarks.SimpleMessage$$anon$6$$anon$1.<init>
[info]   3.9%   7.9% sauerkraut.format.pb.DescriptorBasedProtoReader.pushWithDesc
[info]   3.7%   7.4% com.google.protobuf.CodedInputStream$ArrayDecoder.readInt32
[info]   3.6%   7.1% com.google.protobuf.CodedOutputStream.newInstance
[info]   2.2%   4.4% scala.collection.mutable.Growable.addAll
[info]   1.8%   3.6% com.google.protobuf.Utf8.encode
[info]   1.6%   3.3% sauerkraut.core.PrimitiveWriter.write
[info]   1.5%   3.0% com.google.protobuf.CodedInputStream$ArrayDecoder.readString
[info]   1.5%   2.9% scala.collection.AbstractIterable.<init>
[info]  16.0%  32.1% <other>

## Simple

[info]  12.6%  25.2% sauerkraut.format.pb.RawBinaryPickleReader.push
[info]   6.6%  13.1% scala.collection.mutable.Growable.addAll
[info]   5.2%  10.4% com.google.protobuf.Utf8.encode
[info]   4.6%   9.1% com.google.protobuf.CodedOutputStream$ArrayEncoder.writeStringNoTag
[info]   4.1%   8.1% scala.collection.AbstractIterable.coll
[info]   3.3%   6.6% com.google.protobuf.CodedOutputStream$ArrayEncoder.writeTag
[info]   2.0%   4.0% sauerkraut.benchmarks.generated.SauerkrautProtocolBufferBenchmarks_writeAndReadSimpleMessage_jmhTest.writeAndReadSimpleMessage_avgt_jmhStub
[info]   1.9%   3.7% com.google.protobuf.CodedOutputStream$ArrayEncoder.<init>
[info]   1.6%   3.1% scala.collection.immutable.List.map
[info]   1.6%   3.1% scala.collection.IterableOnceOps.toArray$
[info]   6.7%  13.4% <other>
```

It looks like sauerkraut is suffering from inefficient collection handling.

One obvious issue is the optimisation for collection of primitives down in protocol buffers.  Previously, scala-pickling treated `Array[Int | Long | Double | Float]` specially, and this is certainly something we should do.  Indeed,  Sauerkraut is unable to parse [packed repeated fields](https://developers.google.com/protocol-buffers/docs/encoding#packed) coming out of pb.

Another potential optimisation would be determining more efficient buffering + collection loading.  Currently writing a collection first requires a `size` to be passed into the interface.  We can expand `CollectionBuilder` to be able to pre-allocate collection buffer space using this size if it's been written in the format.  Such a method would be an optional optimisation, and merits performance investigation.  The complication here, e.g. is that formats like protocol buffers would still require *multiple* possible collection encodings to be aggregated together.

## Stack Tracing Results (post-knownFields-opt 2020-03-01)

We did some minor refcatoring to knownField iteration so that we store it in an ARRAY, rather than a list.
This gives us more efficient index/size operations (which we were using).

Then we run some stack debugging.  We find `DataOutputStream`/`DataInputStream` as the primary contributors for the NBT format.
would like for this to be the case in all of our serialization, but the overhead seems particularly large.  Likely need to
invest in custom input/output logic if we want to bring NBT up to speed.

sauerkraut nbt format:
```
[info]  16.6%  33.2% java.io.DataOutputStream.write
[info]   5.8%  11.6% java.io.DataInputStream.readFully
[info]   5.3%  10.6% java.io.DataInputStream.readUnsignedShort
[info]   3.8%   7.6% java.io.DataInputStream.readUTF
[info]   3.6%   7.1% sauerkraut.format.nbt.NbtPickleReader.readPrimitive
[info]   2.6%   5.1% java.io.DataOutputStream.writeByte
[info]   2.1%   4.2% sauerkraut.benchmarks.SimpleMessage$$anon$1.write
[info]   1.5%   3.0% sauerkraut.benchmarks.generated.NbtBenchmarks_writeAndReadSimpleMessage_jmhTest.writeAndReadSimpleMessage_avgt_jmhStub
[info]   1.2%   2.4% scala.collection.IterableOnceOps.toArray$
[info]   1.1%   2.2% sauerkraut.format.nbt.NbtPickleReader.readStructure
[info]   6.5%  13.1% <other>
```

The protocol buffer based binary format does much better in the benchmarks.   There's a few things that call out as areas of investigationg,
for example why are we calling toArray on IterableOnceOps in the hot path?  (likely tuple-ification of structure creation).

sauerkraut RawBinary format:
```
[info]   8.7%  17.4% scala.collection.StrictOptimizedLinearSeqOps.drop
[info]   6.8%  13.7% com.google.protobuf.Utf8.encode
[info]   6.8%  13.6% sauerkraut.format.pb.RawBinaryPickleReader.push
[info]   5.4%  10.8% com.google.protobuf.CodedOutputStream$ArrayEncoder.writeStringNoTag
[info]   4.4%   8.8% com.google.protobuf.CodedOutputStream$ArrayEncoder.writeTag
[info]   3.4%   6.8% sauerkraut.format.pb.RawBinaryFieldWriter.<init>
[info]   2.4%   4.8% sauerkraut.benchmarks.generated.RawBinaryBenchmarks_writeAndReadSimpleMessage_jmhTest.writeAndReadSimpleMessage_avgt_jmhStub
[info]   2.2%   4.4% scala.collection.IterableOnceOps.toArray$
[info]   1.7%   3.4% scala.collection.mutable.Growable.addAll
[info]   1.5%   3.1% sauerkraut.benchmarks.SimpleMessage$$anon$1.write
[info]   6.6%  13.3% <other>
```


## Stack Tracing Results (initial 2020-03-01)

This is checking the RawBinary format specifically for where slowdown vs. native Java protocol buffers is happening.

We find a huge slowdown from looking up the length of an immutable list.  It turns out, this is all from calculating the number
of fields on a given structure.  Additionally, we see a lot of overhead on boxing integers.
```
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖stack":
[info] Stack profiler:
[info] ....[Thread state distributions]....................................................................
[info]  50.0%         RUNNABLE
[info]  50.0%         TIMED_WAITING
[info] ....[Thread state: RUNNABLE]........................................................................
[info]  11.4%  22.8% scala.collection.immutable.List.length
[info]   6.0%  12.0% com.google.protobuf.Utf8.encode
[info]   5.9%  11.8% sauerkraut.benchmarks.SimpleMessage$$anon$1.write
[info]   5.5%  11.0% com.google.protobuf.CodedInputStream.newInstance
[info]   3.7%   7.4% sauerkraut.format.pb.RawBinaryPickleReader.push
[info]   2.5%   5.0% scala.runtime.BoxesRunTime.boxToInteger
[info]   2.4%   4.8% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.bufferUInt32NoTag
[info]   2.3%   4.6% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.<init>
[info]   1.4%   2.9% scala.deriving$.productElement
[info]   1.1%   2.3% sauerkraut.benchmarks.generated.RawBinaryBenchmarks_writeAndReadSimpleMessage_jmhTest.writeAndReadSimpleMessage_avgt_jmhStub
[info]   7.7%  15.4% <other>
```