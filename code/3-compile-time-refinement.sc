import eu.timepit.refined.api.Refined
import eu.timepit.refined.refineMV
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.types.numeric.NonNegInt
import eu.timepit.refined.types.string.NonEmptyString

refineMV[NonEmpty]("foo")  // explicit refinement
NonEmptyString("bar") // alternative explicit refinement using companion object's `RefinedTypeOps`
NonEmptyString("")  // fails at compile time!

import eu.timepit.refined.auto._  // enables auto-refinement
import eu.timepit.refined.numeric.Positive

val x: Int Refined Positive = 3  // see also built-in `PosInt` alias
x.value

import eu.timepit.refined.string.Url

//val y: String Refined Url = "htp://example.com"
// Error:(8, 29) Url predicate failed: unknown protocol: htp


def sayHello(times: NonNegInt): String = "hello" * times.value

sayHello(2)

//sayHello(-5)
//Error:(31, 10) Predicate (-5 < 0) did not fail.
