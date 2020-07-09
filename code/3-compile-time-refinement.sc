import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.numeric.{NonNegBigDecimal, PosInt}
import eu.timepit.refined.auto._
import eu.timepit.refined.numeric.Greater
import eu.timepit.refined.string.Url

val x: PosInt = 3  // alias for `Int Refined Positive`
//val y: String Refined Url = "htp://example.com"
// Error:(8, 29) Url predicate failed: unknown protocol: htp
val z: NonNegBigDecimal = BigDecimal(2)