import cats.data.NonEmptyList
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.{Digit, LetterOrDigit}
import eu.timepit.refined.collection.{Forall, MaxSize, NonEmpty, Size}
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.string.MatchesRegex

type AccountNumber = String Refined (Size[Equal[8]] And Forall[Digit])
type Username = String Refined (NonEmpty And MaxSize[20] And Forall[LetterOrDigit])
type EmailAddress = String Refined MatchesRegex[
  "^[a-zA-Z0-9.]+@[a-zA-Z0-9]+\\.[a-zA-Z]+$" // simplified regex!
]
type Score = Int Refined Interval.Closed[0, 100]

object AccountNumber extends RefinedTypeOps[AccountNumber, String]
object Username extends RefinedTypeOps[Username, String]
object EmailAddress extends RefinedTypeOps[EmailAddress, String]
object Score extends RefinedTypeOps[Score, Int]

final case class Account(
  accountNumber: AccountNumber,
  username: Username,
  email: EmailAddress,
  score: Score
)

import cats.syntax.either._  // for `toEitherNel`
import cats.syntax.parallel._  // for `parMapN`

def makeAccount(
  accountNumber: String,
  username: String,
  email: String,
  score: Int
): Either[NonEmptyList[String], Account] =
  (
    AccountNumber.from(accountNumber).leftMap("invalid AccountNumber: " + _).toEitherNel,
    Username.from(username).leftMap("invalid Username: " + _).toEitherNel,
    EmailAddress.from(email).leftMap("invalid EmailAddress: " + _).toEitherNel,
    Score.from(score).leftMap("invalid Score: " + _).toEitherNel,
  ).parMapN(Account)

makeAccount("00000042", "Cloud", "cloud@avalanche.com", 100)

makeAccount("58", "!*Invalid™", "woops", -3)