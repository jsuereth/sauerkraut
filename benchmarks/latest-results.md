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

| Benchmark | Format    | ByteSize | ns / op   | error (ns / op) |
| --------- | --------- | -------- | --------- | --------------- |
| simple    | JavaProto |	1550     | 419.798   | 4.919           |
| simple    | proto     | 1550     | 428.289   | 4.385           |
| simple    | raw       | 1550     | 442.104   | 3.000           |
| simple    | nbt       | 2000     | 746.992   | 9.415           |
| simple    | json      | 2175     | 1562.585  | 15.149          |
| simple    | JavaSer   | 3900     | 3365.595  | 203.147         |
| simple    | xml       | 4775     | 25715.052 | 452.214         |
| complex   | JavaProto | 4025     | 902.756   | 19.716          |
| complex   | raw       | 4675     | 4574.543  | 25.797          |
| complex   | json      | 5875     | 7761.684  | 204.940         |
| complex   | nbt       | 7350     | 3933.956  | 67.111          |
| complex   | JavaSer   | 25600    | 31395.595 | 203.147         |
| complex   | xml       | 32125    | 44126.052 | 452.214         |

Currently there's a bit too much overhead in Sauerkraut vs. Java's protocol
buffers. It still beats Java Serialization on all counts, but that's
nothing impressive in today's world.

Let's look into likely suspects for this 2x overhead on simple messages.

Note: For complex messages our overhead in ByteSize is likely due to not
treating collections of primitives specially, which also accounts for a
large runtime overhead.

## Areas to investigate:

- [X] Figure out where immutable.List.length is being called from (22.8%)
  - `Field.unapply` in RawBinaryReader was the issue.
  - We were doing validation that tags read MATCH what we see in
    the type.  May be more efficient just to let it throw and catch
    later.   We'll figure out more when we update error reporting.
- [ ] Figure out where `StrictOptimisedLinearSeqOps.drop` is being called.
- [ ] Split apart `RawBinaryPickleReader.push` (7.4%)
- [ ] Attempt to remove the crazy amount of boxing/unboxing we have w/ Primitives (2.5%)
  - [ ] Erase PrimitiveBuilders in codegen of `Buildable.derived[T]`
  - [ ] Add direct un-boxed signatures for putPrimitive.
- [ ] Figure out if `Mirror.ProductOf[T]#fromProduct` could be more efficient. (2.9%)


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