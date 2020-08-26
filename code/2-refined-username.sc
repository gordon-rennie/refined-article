import eu.timepit.refined.collection.{MaxSize, NonEmpty}
import eu.timepit.refined.types.string.NonEmptyString

NonEmptyString.from("")

NonEmptyString.from("hello")


import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.Forall

type Username = String Refined (NonEmpty And MaxSize[20] And Forall[LetterOrDigit])

object Username extends RefinedTypeOps[Username, String]  // "free" smart constructor amd more!

Username.from("Tito")

Username.from("")