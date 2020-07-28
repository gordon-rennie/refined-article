import cats.data.NonEmptyList
import cats.effect.Bracket
import doobie.util.transactor.Transactor
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.{Digit, LetterOrDigit}
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.generic.Equal
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.numeric.Interval.OpenClosed
import eu.timepit.refined.string.MatchesRegex

type AccountNumber = String Refined (Size[Equal[8]] And Forall[Digit])
type Username = String Refined (Forall[LetterOrDigit] And Size[OpenClosed[0, 20]])
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

// recall
// final case class Account(
//   accountNumber: AccountNumber,
//   username: Username,
//   email: EmailAddress,
//   score: Score
// )

import doobie.implicits._
import doobie.refined.implicits._

final class AccountRepository[F[_]: Bracket[*[_], Throwable]](transactor: Transactor[F]) {

  def find(accountNumber: AccountNumber): F[Option[Account]] = {
    sql"""SELECT account_number,
         |       username,
         |       email,
         |       score
         |FROM accounts
         |WHERE account_number=$accountNumber
         |""".stripMargin
      .query[Account]
      .option
      .transact(transactor)
  }
}