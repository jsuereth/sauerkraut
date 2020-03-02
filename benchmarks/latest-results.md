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

## Full Results

| Benchmark | Format    | ByteSize | ns / op   | error (ns / op) |
| --------- | --------- | -------- | --------- | --------------- |
| simple    | JavaProto |	1550     | 423.42    | 3.223           |
| simple    | nbt       | 2000     | 763.427   | 11.281          |
| simple    | raw       | 1550     | 903.28    | 4.169           |
| simple    | json      | 2175     | 1552.229  | 16.216          |
| simple    | JavaSer   | 3900     | 3342      | 67.027          |
| complex   | JavaProto | 4025     | 1062.874  | 24.593          |
| complex   | nbt       | 7350     | 3869.315  | 36.619          |
| complex   | raw       | 4675     | 5629.863  | 29.099          |
| complex   | json      | 5875     | 8207.055  | 123.497         |
| complex   | JavaSer   | 25600    | 31522.989 | 471.474         |

Currently there's a bit too much overhead in Sauerkraut vs. Java's protocol
buffers. It still beats Java Serialization on all counts, but that's
nothing impressive in today's world.

Let's look into likely suspects for this 2x overhead on simple messages.

Note: For complex messages our overhead in ByteSize is likely due to not
treating collections of primitives specially, which also accounts for a
large runtime overhead.

## Stack Tracing Results

This is checking the RawBinary format specifically for
where slowdown vs. native Java protocol buffers is
happening.
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

Areas to investigate:

- [ ] Figure out where immutable.List.length is being called from (22.8%)
- [ ] Split apart `RawBinaryPickleReader.push` (7.4%)
- [ ] Attempt to remove the crazy amount of boxing/unboxing we have w/ Primitives (2.5%)
  - [ ] Erase PrimitiveBuilders in codegen of `Buildable.derived[T]`
  - [ ] Add direct un-boxed signatures for putPrimitive.
- [ ] Figure out if `Mirror.ProductOf[T]#fromProduct` could be more efficient. (2.9%)