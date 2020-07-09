import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.{AllOf, And}
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, NonEmpty, Size}
import eu.timepit.refined.numeric.Interval.OpenClosed
import eu.timepit.refined.types.string.NonEmptyString

NonEmptyString.from("")

NonEmptyString.from("hello")

type Username = String Refined And[
  Forall[LetterOrDigit],  // all chars must meet sub-predicate `LetterOrDigit`
  Size[OpenClosed[0, 20]] // size must be gt 0, leq 20
]

object Username extends RefinedTypeOps[Username, String]  // "free" smart constructor amd more!

Username.from("Bob")

Username.from("")