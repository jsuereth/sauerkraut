```
[info] # Fork: 5 of 5
[info] # Warmup Iteration   1: 0.001 ms/op
[info] # Warmup Iteration   2: 0.001 ms/op
[info] # Warmup Iteration   3: 0.001 ms/op
[info] # Warmup Iteration   4: 0.001 ms/op
[info] # Warmup Iteration   5: 0.001 ms/op
[info] Iteration   1: 0.001 ms/op
[info]                  bytesWritten:                 62.000 #
[info]                  ╖gc.alloc.rate:               6916.223 MB/sec
[info]                  ╖gc.alloc.rate.norm:          9376.000 B/op
[info]                  ╖gc.churn.G1_Eden_Space:      6955.553 MB/sec
[info]                  ╖gc.churn.G1_Eden_Space.norm: 9429.317 B/op
[info]                  ╖gc.churn.G1_Old_Gen:         0.012 MB/sec
[info]                  ╖gc.churn.G1_Old_Gen.norm:    0.017 B/op
[info]                  ╖gc.count:                    118.000 counts
[info]                  ╖gc.time:                     132.000 ms
[info]                  ╖stack:                       <delayed till summary>
[info] Iteration   2: 0.001 ms/op
[info]                  bytesWritten:                 62.000 #
[info]                  ╖gc.alloc.rate:               6317.811 MB/sec
[info]                  ╖gc.alloc.rate.norm:          9376.000 B/op
[info]                  ╖gc.churn.G1_Eden_Space:      6366.036 MB/sec
[info]                  ╖gc.churn.G1_Eden_Space.norm: 9447.569 B/op
[info]                  ╖gc.churn.G1_Old_Gen:         0.010 MB/sec
[info]                  ╖gc.churn.G1_Old_Gen.norm:    0.015 B/op
[info]                  ╖gc.count:                    108.000 counts
[info]                  ╖gc.time:                     124.000 ms
[info]                  ╖stack:                       <delayed till summary>
[info] Iteration   3: 0.001 ms/op
[info]                  bytesWritten:                 62.000 #
[info]                  ╖gc.alloc.rate:               6317.259 MB/sec
[info]                  ╖gc.alloc.rate.norm:          9376.000 B/op
[info]                  ╖gc.churn.G1_Eden_Space:      6366.085 MB/sec
[info]                  ╖gc.churn.G1_Eden_Space.norm: 9448.468 B/op
[info]                  ╖gc.churn.G1_Old_Gen:         0.012 MB/sec
[info]                  ╖gc.churn.G1_Old_Gen.norm:    0.018 B/op
[info]                  ╖gc.count:                    108.000 counts
[info]                  ╖gc.time:                     128.000 ms
[info]                  ╖stack:                       <delayed till summary>
[info] Iteration   4: 0.001 ms/op
[info]                  bytesWritten:                 62.000 #
[info]                  ╖gc.alloc.rate:               6615.840 MB/sec
[info]                  ╖gc.alloc.rate.norm:          9376.000 B/op
[info]                  ╖gc.churn.G1_Eden_Space:      6660.422 MB/sec
[info]                  ╖gc.churn.G1_Eden_Space.norm: 9439.182 B/op
[info]                  ╖gc.churn.G1_Old_Gen:         0.012 MB/sec
[info]                  ╖gc.churn.G1_Old_Gen.norm:    0.017 B/op
[info]                  ╖gc.count:                    113.000 counts
[info]                  ╖gc.time:                     137.000 ms
[info]                  ╖stack:                       <delayed till summary>
[info] Iteration   5: 0.001 ms/op
[info]                  bytesWritten:                 62.000 #
[info]                  ╖gc.alloc.rate:               6548.020 MB/sec
[info]                  ╖gc.alloc.rate.norm:          9376.000 B/op
[info]                  ╖gc.churn.G1_Eden_Space:      6602.067 MB/sec
[info]                  ╖gc.churn.G1_Eden_Space.norm: 9453.390 B/op
[info]                  ╖gc.churn.G1_Old_Gen:         0.012 MB/sec
[info]                  ╖gc.churn.G1_Old_Gen.norm:    0.017 B/op
[info]                  ╖gc.count:                    112.000 counts
[info]                  ╖gc.time:                     134.000 ms
[info]                  ╖stack:                       <delayed till summary>
[info] Result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage":
[info]   0.001 ▒(99.9%) 0.001 ms/op [Average]
[info]   (min, avg, max) = (0.001, 0.001, 0.002), stdev = 0.001
[info]   CI (99.9%): [0.001, 0.001] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:bytesWritten":
[info]   1550.000 ▒(99.9%) 0.001 # [Sum]
[info]   (min, avg, max) = (62.000, 62.000, 62.000), stdev = 0.001
[info]   CI (99.9%): [1550.000, 1550.000] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate":
[info]   6032.207 ▒(99.9%) 463.813 MB/sec [Average]
[info]   (min, avg, max) = (5302.226, 6032.207, 7048.150), stdev = 619.177
[info]   CI (99.9%): [5568.395, 6496.020] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate.norm":
[info]   9369.600 ▒(99.9%) 9.786 B/op [Average]
[info]   (min, avg, max) = (9344.000, 9369.600, 9376.000), stdev = 13.064
[info]   CI (99.9%): [9359.814, 9379.386] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space":
[info]   6071.900 ▒(99.9%) 472.169 MB/sec [Average]
[info]   (min, avg, max) = (5328.292, 6071.900, 7071.162), stdev = 630.333
[info]   CI (99.9%): [5599.731, 6544.070] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space.norm":
[info]   9430.238 ▒(99.9%) 31.380 B/op [Average]
[info]   (min, avg, max) = (9308.569, 9430.238, 9487.313), stdev = 41.892
[info]   CI (99.9%): [9398.858, 9461.618] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen":
[info]   0.012 ▒(99.9%) 0.001 MB/sec [Average]
[info]   (min, avg, max) = (0.008, 0.012, 0.016), stdev = 0.002
[info]   CI (99.9%): [0.010, 0.013] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen.norm":
[info]   0.018 ▒(99.9%) 0.002 B/op [Average]
[info]   (min, avg, max) = (0.014, 0.018, 0.023), stdev = 0.002
[info]   CI (99.9%): [0.016, 0.020] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.count":
[info]   3000.000 ▒(99.9%) 0.001 counts [Sum]
[info]   (min, avg, max) = (101.000, 120.000, 136.000), stdev = 11.442
[info]   CI (99.9%): [3000.000, 3000.000] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.time":
[info]   2992.000 ▒(99.9%) 0.001 ms [Sum]
[info]   (min, avg, max) = (92.000, 119.680, 149.000), stdev = 17.886
[info]   CI (99.9%): [2992.000, 2992.000] (assumes normal distribution)
[info] Secondary result "sauerkraut.benchmarks.RawBinaryBenchmarks.writeAndReadSimpleMessage:╖stack":
[info] Stack profiler:
[info] ....[Thread state distributions]....................................................................
[info]  50.0%         RUNNABLE
[info]  50.0%         TIMED_WAITING
[info] ....[Thread state: RUNNABLE]........................................................................
[info]  10.2%  20.4% scala.collection.immutable.List.length
[info]   6.4%  12.7% sauerkraut.benchmarks.SimpleMessage$$anon$1.write
[info]   5.9%  11.8% com.google.protobuf.Utf8.encode
[info]   5.8%  11.5% com.google.protobuf.CodedInputStream.newInstance
[info]   3.6%   7.2% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.bufferUInt32NoTag
[info]   3.4%   6.8% sauerkraut.format.pb.RawBinaryPickleReader.push
[info]   2.3%   4.5% sauerkraut.format.pb.RawBinaryFieldWriter.<init>
[info]   1.8%   3.5% com.google.protobuf.CodedOutputStream$AbstractBufferedEncoder.<init>
[info]   1.3%   2.6% scala.collection.IterableOnceOps.toArray$
[info]   1.1%   2.2% sauerkraut.benchmarks.generated.RawBinaryBenchmarks_writeAndReadSimpleMessage_jmhTest.writeAndReadSimpleMessage_avgt_jmhStub
[info]   8.3%  16.7% <other>
[info] ....[Thread state: TIMED_WAITING]...................................................................
[info]  50.0% 100.0% java.lang.Object.wait
[info] # Run complete. Total time: 00:52:44
[info] REMEMBER: The numbers below are just data. To gain reusable insights, you need to follow up on
[info] why the numbers are the way they are. Use profilers (see -prof, -lprof), design factorial
[info] experiments, perform baseline and negative tests that provide experimental control, make sure
[info] the benchmarking environment is safe on JVM/OS/HW level, ask for reviews from the domain experts.
[info] Do not assume the numbers tell you what you want them to tell.
[info] Benchmark                                                                        Mode  Cnt      Score     Error   Units
[info] JsonBenchmarks.writeAndReadLargeNestedMessage                                    avgt   25      0.011 ▒   0.002   ms/op
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:bytesWritten                       avgt   25   5875.000                 #
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate                     avgt   25   1947.403 ▒ 325.189  MB/sec
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate.norm                avgt   25  23105.601 ▒  89.422    B/op
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space            avgt   25   1957.631 ▒ 326.828  MB/sec
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space.norm       avgt   25  23226.866 ▒ 156.505    B/op
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen               avgt   25      0.010 ▒   0.002  MB/sec
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen.norm          avgt   25      0.122 ▒   0.018    B/op
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.count                          avgt   25   1837.000            counts
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖gc.time                           avgt   25   1871.000                ms
[info] JsonBenchmarks.writeAndReadLargeNestedMessage:╖stack                             avgt             NaN               ---
[info] JsonBenchmarks.writeAndReadSimpleMessage                                         avgt   25      0.003 ▒   0.001   ms/op
[info] JsonBenchmarks.writeAndReadSimpleMessage:bytesWritten                            avgt   25   2175.000                 #
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate                          avgt   25   3285.522 ▒ 247.196  MB/sec
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate.norm                     avgt   25  10446.400 ▒   9.786    B/op
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space                 avgt   25   3299.603 ▒ 247.470  MB/sec
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space.norm            avgt   25  10491.570 ▒  42.545    B/op
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen                    avgt   25      0.008 ▒   0.001  MB/sec
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen.norm               avgt   25      0.027 ▒   0.004    B/op
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.count                               avgt   25   2548.000            counts
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖gc.time                                avgt   25   1763.000                ms
[info] JsonBenchmarks.writeAndReadSimpleMessage:╖stack                                  avgt             NaN               ---
[info] NbtBenchmarks.writeAndReadLargeNestedMessage                                     avgt   25      0.007 ▒   0.001   ms/op
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:bytesWritten                        avgt   25   7350.000                 #
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate                      avgt   25    861.934 ▒  20.489  MB/sec
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate.norm                 avgt   25   7052.800 ▒  70.121    B/op
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space             avgt   25    865.111 ▒  21.825  MB/sec
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space.norm        avgt   25   7078.791 ▒  91.196    B/op
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen                avgt   25      0.001 ▒   0.001  MB/sec
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen.norm           avgt   25      0.011 ▒   0.003    B/op
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.count                           avgt   25   1248.000            counts
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖gc.time                            avgt   25    822.000                ms
[info] NbtBenchmarks.writeAndReadLargeNestedMessage:╖stack                              avgt             NaN               ---
[info] NbtBenchmarks.writeAndReadSimpleMessage                                          avgt   25      0.001 ▒   0.001   ms/op
[info] NbtBenchmarks.writeAndReadSimpleMessage:bytesWritten                             avgt   25   2000.000                 #
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate                           avgt   25   1181.942 ▒ 109.911  MB/sec
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate.norm                      avgt   25   1528.000 ▒   3.868    B/op
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space                  avgt   25   1185.074 ▒ 110.987  MB/sec
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space.norm             avgt   25   1531.927 ▒   8.291    B/op
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen                     avgt   25      0.004 ▒   0.001  MB/sec
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen.norm                avgt   25      0.004 ▒   0.001    B/op
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.count                                avgt   25   1502.000            counts
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖gc.time                                 avgt   25   1073.000                ms
[info] NbtBenchmarks.writeAndReadSimpleMessage:╖stack                                   avgt             NaN               ---
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage                               avgt   25      0.012 ▒   0.001   ms/op
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:bytesWritten                  avgt   25   4675.000                 #
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate                avgt   25   1916.577 ▒  96.391  MB/sec
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.alloc.rate.norm           avgt   25  24422.401 ▒  98.879    B/op
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space       avgt   25   1924.650 ▒  97.325  MB/sec
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Eden_Space.norm  avgt   25  24524.898 ▒ 104.954    B/op
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen          avgt   25      0.008 ▒   0.002  MB/sec
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.churn.G1_Old_Gen.norm     avgt   25      0.096 ▒   0.022    B/op
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.count                     avgt   25   2325.000            counts
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖gc.time                      avgt   25   1631.000                ms
[info] RawBinaryBenchmarks.writeAndReadLargeNestedMessage:╖stack                        avgt             NaN               ---
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage                                    avgt   25      0.001 ▒   0.001   ms/op
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:bytesWritten                       avgt   25   1550.000                 #
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate                     avgt   25   6032.207 ▒ 463.813  MB/sec
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.alloc.rate.norm                avgt   25   9369.600 ▒   9.786    B/op
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space            avgt   25   6071.900 ▒ 472.169  MB/sec
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Eden_Space.norm       avgt   25   9430.238 ▒  31.380    B/op
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen               avgt   25      0.012 ▒   0.001  MB/sec
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.churn.G1_Old_Gen.norm          avgt   25      0.018 ▒   0.002    B/op
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.count                          avgt   25   3000.000            counts
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖gc.time                           avgt   25   2992.000                ms
[info] RawBinaryBenchmarks.writeAndReadSimpleMessage:╖stack                             avgt             NaN               ---
[success] Total time: 3168 s (52:48), completed Feb 29, 2020, 11:49:53 PM
```