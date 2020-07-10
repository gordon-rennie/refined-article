import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.Digit
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.Interval

type AccountNumber = String Refined (Size[Equal[8]] And Forall[Digit])
type Score = Int Refined Interval.Closed[0, 100]

object AccountNumber extends RefinedTypeOps[AccountNumber, String]
object Score extends RefinedTypeOps[Score, Int]

import io.circe.Decoder
import io.circe.generic._
import io.circe.numbers._

final case class RawRequest(
  accountNumber: String,
  score: Int
)

val rawDecoder: Decoder[RawRequest] = Decoder.forProduct2("account_number", "score")(RawRequest)

final case class RefinedRequest(
  accountNumber: AccountNumber,
  score: Score
)

//import io.circe.refined._
implicit val test: Decoder[AccountNumber] = Decoder.decodeString.emap(AccountNumber.from)

val refinedDecoder: Decoder[RefinedRequest] = {
  Decoder.forProduct2("account_number", "score")(RefinedRequest)
}
