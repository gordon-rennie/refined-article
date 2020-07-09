// TurboBoost disabled
// scalaVersion = 2.13.3
// `hot` warmup mode
// run `sbt` in scala/compiler-benchmark and run `hot -psource=/my/home/dir/compiler-benchmark/corpus/1-no-refinement-explicit`

// 1-no-refinement-explicit
// [info] Iteration  10: 27.017 ±(99.9%) 0.331 ms/op
// [info]                  compile·p0.00:   26.083 ms/op
// [info]                  compile·p0.50:   26.575 ms/op
// [info]                  compile·p0.90:   27.427 ms/op
// [info]                  compile·p0.95:   28.294 ms/op
// [info]                  compile·p0.99:   39.013 ms/op
// [info]                  compile·p0.999:  40.042 ms/op
// [info]                  compile·p0.9999: 40.042 ms/op
// [info]                  compile·p1.00:   40.042 ms/op