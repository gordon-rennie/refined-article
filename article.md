# Safe, Expressive Code with Refinement Types

We devote a significant amount of time and attention as developers to ensuring that the data we receive and process is valid. We might enforce that usernames fall within a character limit, reject malformed URLs, guard against paying negative values, or countless more domain-specific cases. In these examples, the type system could not eliminate the errors: we may accept a parameter `username: String`, but we likely have no intention of allowing the empty string, strange Unicode glyphs, or strings thousands of characters in length!

In this blog post I will explore _refinement types_ and their implementation in Scala's [`refined` library](https://github.com/fthomas/refined). Refinement types are a powerful tool for restricting the values passed into our logic, which help us fail fast and gracefully on invalid values. I'd particularly like to highlight the excellent interoperability of `refined` with Scala’s libraries, and how this can make our programs safer and easier to reason about for very little effort. 

## Conventional Validation

We are familiar with the notion of guards in our logic to defend against bad inputs. In Java, the convention is to carefully specify a method's contract in its Javadoc, and throw an `IllegalArgumentException` if any inputs are invalid. In functional Scala, we prefer to return our result as an `Option` or `Either`: 

```scala
def validateUser(username: String): Either[String, String] =
    if (username.isEmpty)
      Left("Username cannot be empty")
    else if (username.length > 20)
      Left("Username must be 20 characters or less")
    else if (!username.forall(_.isLetterOrDigit))
      Left("Usernames can only contain alphanumeric characters")
    else
      Right(username)

$ validateUser("xX_ŚePhI®OtH_Xx")
| val res0: Either[String,String] = Left(Usernames can only contain alphanumeric characters)

$ validateUser("Luna")
| val res1: Either[String,String] = Right(Luna)
```

There are a couple of drawbacks with this approach:

* successfully validated data remains typed as a `String`; we've "thrown away" the potentially useful information that it's an alphanumeric string of one to 20 characters
* we've had to write and test the validation code, and will have plenty more similar code to write as our domain grows

Lets see if we can improve the situation; enter *refinement types*!

## Refinement Types

> In type theory, a refinement type is a type endowed with a predicate which is assumed to hold for any element of the refined type.
>
> &mdash; the original [Haskell `refined` library](https://github.com/nikita-volkov/refined)

The building blocks are simple: 

* given some type `T` 
* and a predicate test expressed as a type `P`
* then our refinement type is `T Refined P`, and it contains only the values in `T` for which `P` is true

Scala's `refined` library provides this functionality, and has a large number of built-in predicate types that we can use for our `P`. For example, a frequently useful refinement type is for a non-empty string (`type NonEmptyString = String Refined NonEmpty`), which is predefined for us and comes with its own `from` smart constructor:

```scala
import eu.timepit.refined.types.string.NonEmptyString

$ NonEmptyString.from("")
| val res0: Either[String,NonEmptyString] = Left(Predicate isEmpty() did not fail.)

$ NonEmptyString.from("hello")
| val res1: Either[String,NonEmptyString] = Right(hello)
```

`refined` provides a great variety of predicates `P` to work with out-of-the-box, including:

* boolean: `Not[P]`, `Or[P1, P2]`, `And[P1, P2]`, `AllOf[Ps]`, `AnyOf[Ps]`
* numeric: `Greater[x]`, `LessEqual[x]`,  `interval.OpenClosed[xMin, xMax]`
* collections: `MinSize[x]`, `Forall[P]`, `Exists[P]`
* strings: `EndsWith[s]`, `MatchesRegex[s]`, `Url`, `ValidFloat`

(The full selection can be found in [the documentation](https://github.com/fthomas/refined))

From this rich set of predicates we can express very diverse constraints. Let's revisit our `username` example:

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.numeric.Interval

type Username = String Refined And[
  Forall[LetterOrDigit],  // all chars must meet sub-predicate `LetterOrDigit`
  Size[Interval.OpenClosed[0, 20]] // size must be gt 0, leq 20
]

object Username extends RefinedTypeOps[Username, String]  // gives us a `.from` smart constructor and more!

$ Username.from("Tito")
| val res2: Either[String,Username] = Right(Tito)

$ Username.from("")
| val res3: Either[String,Username] = Left(Right predicate of (() && ((0 > 0) && !(0 > 20))) failed: Predicate taking size() = 0 failed: Left predicate of ((0 > 0) && !(0 > 20)) failed: Predicate failed: (0 > 0).)
```

Our refinement type `Username` and its smart constructor have replaced the validation logic we wrote previously. What have we gained by doing this?

* our validations rules are expressed declaratively as a predicate in the type alias definition
* the return type is our `Username` alias: we’ve retained the information that the base `String` has been validated, and have a contextually useful name
* we were given validation logic and error reporting essentially for free
  * The error messages are clearly quite robotic! They are generally sufficient for developers, and can be adapted to be more pleasant for end users when needed.


We gained this with no runtime cost; our compiled bytecode will see only a `String`. (For refinements on [value types](https://docs.scala-lang.org/tour/unified-types.html) such as `Int`, we would incur only the small cost of boxing it). 

Let's look at some more examples, and how our refinement types can be used with other libraries.

> **_Note:_** It might seem strange seeing literal values in your predicate types, as in `Int Refined GreaterEqual[0]`. Under the hood, the value `0` is being treated as a [literal-based singleton type](https://docs.scala-lang.org/sips/42.type.html), newly supported in Scala 2.13.
>
> If you are on a pre-2.13 version of Scala,  you must use a `shapeless` implementation instead, which is slightly less readable (but perfectly fine!): ``Int Refined GreaterEqual[W.`0`.T]`` — refer to the [`refined` docs](https://github.com/fthomas/refined).

> **_Tip:_** We can take advantage of Scala’s postfix notation support for better readability in our types. In fact, we already have: `refined`’s type definitions of the form `T Refined P` are equivalent to `Refined[T, P]`. Similarly, we could rewrite our previous example as `type Username = String Refined (Forall[LetterOrDigit] And Size[OpenClosed[0, 20]])`

## Refined In Action

### Compile Time Refinement

`refined` has a nifty super power: we can convert values of any literal, `BigDecimal` or `BigInt` at compile time to their refinement type, and any failures will emit a compiler error. If the predicate passes, we receive the refined value directly, not wrapped inside an `Either`! This is great for hard-coded values in configuration or test fixtures. We can either perform the refinement explicitly, like this:

```scala
import eu.timepit.refined.refineMV
import eu.timepit.refined.collection.NonEmpty

$ refineMV[NonEmpty]("foo")  // explicit refinement
| val res0: Refined[String,NonEmpty] = foo

$ NonEmptyString("bar") // alternative explicit refinement using companion object's `apply` from `RefinedTypeOps`
| val res1: NonEmptyString = bar  // alias expands to Refined[String,NonEmpty], as above

$ NonEmptyString("")  // fails at compile time!
| Error:(9, 15) Predicate isEmpty() did not fail.
```

… or we can enable implicit auto-refinement using an import:

```scala
import eu.timepit.refined.auto._  // enables auto-refinement
import eu.timepit.refined.numeric.Positive

$ val x: Int Refined Positive = 3
| val x: Int Refined Positive = 3
$ x.value  // .value unwraps any refined value back to the base type
| val res2: Int = 3


import eu.timepit.refined.string.Url

$ val y: String Refined Url = "htp://example.com"
| Error:(8, 29) Url predicate failed: unknown protocol: htp


def sayHello(times: NonNegInt): String =
  "hello" * times.value

$ sayHello(2)  // auto-refined argument!
| val res3: String = hellohello

$ sayHello(-5)
| Error:(31, 10) Predicate (-5 < 0) did not fail.
```

For auto-refinement to work we had to explicitly type our variables, *e.g.* `val x: PosInt` — otherwise the compiler would infer `x` as the base type, `Int`. We also saw that we can unwrap any refined value back to its base type using the `.value` method — we need this when we call code that doesn’t use refinement types in its interface, such as the standard library.

## Runtime Refinement

The majority of data we process is not known at compile time. Rather, it comes from the external boundaries of our services, in the form of web requests, events, files, and database query results. In these cases we must safely convert the incoming data to our refinement types, and gracefully handle invalid values. Let’s firstly look at how we can do this manually, then explore some integration libraries that can do all the heavy lifting for us.

### Manual Validation

Let’s define a toy domain entity, `Account`, with fields with refinement types:

```scala
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
```

Occasionally you will find yourself needing to refine raw values yourself, for example because you are interfacing with part of your codebase that wasn’t written with refinement types. In these situations we have to perform safe conversions to the refinement types, returning an `Either[String, RefinedType]`, compose the success results, and pass them into our logic.

Fortunately, `Either` forms a monad and we can compose them easily in Scala, for example using a for-comprehension. We can do better still using the [`cats` library](https://github.com/typelevel/cats):

```scala
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
    Score.from(score).leftMap("invalid Score: " + _).toEitherNel
  ).parMapN(Account)

$ makeAccount("00000042", "Cloud", "cloud@avalanche.com", 100)
| val res0: Either[cats.data.NonEmptyList[String],Account] = Right(Account(00000042,Cloud,cloud@avalanche.com,100))

$ makeAccount("58", "!*Invalid™", "woops@goggle", -3)
| val res1: Either[cats.data.NonEmptyList[String],Account] = Left(NonEmptyList(invalid AccountNumber: <...>, invalid Username: <...>, invalid EmailAddress: <...>, invalid Score: <...>))
```

This implementation will return a valid `Account` or a list of every error encountered. We have also `leftMap`ped the error channels to provide a more pleasant error message.

> **_Note:_** The usual `Either` behaviour is to fail fast and report only the first error encountered. We have taken advantage of the error-accumulating semantics when using `.parMapN` on a tuple of `Either[NonEmptyList[E], A]`. If you are familiar with `Validated`, this is  equivalent to converting from the `Either` to `ValidatedNel`s and back again — a nice trick I picked up from [a talk by Gabriel Volpe](https://www.youtube.com/watch?v=n1Y2V4zCZdQ).

### Library Integration

The refinement logic we just wrote is okay, but I’ve found the greatest benefits in using refinement types when I can push validation right to the edges of my application and get integration libraries to do the work for me. Let’s look at some examples.

#### JSON Parsing

Consider an example where we want to decode JSON-encoded requests, in this case using the [`circe` JSON library](https://github.com/circe/circe). Taking advantage of Circe’s generic codec derivation, we can already get codecs for requests of base types:

```scala
import io.circe._
import io.circe.generic.semiauto._

final case class RawRequest(
  accountNumber: String,
  score: Int
)

$ val rawDecoder: Decoder[RawRequest] = Decoder.forProduct2("account_number", "score")(RawRequest)
| val rawDecoder: io.circe.Decoder[RawRequest] = io.circe.ProductDecoders$$anon$2@79a6d61c
```

However, we hit a compiler error if we try to use refinement types in our request object:

```scala
final case class RefinedRequest(
  accountNumber: AccountNumber,
  score: Score
)

$ val refinementDecoder: Decoder[RefinedRequest] = Decoder.forProduct2("account_number", "score")(RefinedRequest)
| Error:(31, 49) could not find implicit value for parameter decodeA0: io.circe.Decoder[AccountNumber]
```

The cause of the error is that Circe doesn’t have the required typeclass instances for our refinement types to do its job. This is easily fixed: Circe has a `circe-refined` module, which will take any codec for a base type (such as `Decoder[Int]`) and implicitly transform it to a refinement type codec (such as `Decoder[PosInt]`). Any refinement failures are converted into the usual Circe parsing error:

```scala
import io.circe.refined._

$ val refinementDecoder: Decoder[RefinedRequest] =
  Decoder.forProduct2("account_number", "score")(RefinedRequest)
| val refinementDecoder: io.circe.Decoder[RefinedRequest] = io.circe.ProductDecoders$$anon$2@6813486c

import io.circe.literal._  // for `json` interpolator

val validRequest = json"""{"account_number" : "12345678", "score" : 55 }"""
val badRequest = json"""{"account_number" : "13", "score" : 55 }"""

// success!
$ refinementDecoder.decodeJson(validRequest)
| val res0: io.circe.Decoder.Result[RefinedRequest] = Right(RefinedRequest(12345678,55))

// rejected
$ refinementDecoder.decodeJson(badRequest)
| val res1: io.circe.Decoder.Result[RefinedRequest] = Left(DecodingFailure(Left predicate of ((2 == 8) && (isDigit('1') && isDigit('3'))) failed: Predicate taking size(13) = 2 failed: Predicate failed: (2 == 8)., List(DownField(account_number))))
```

We used `circe` in our example, but integration libraries are also available for other JSON libraries including [spray-json](https://index.scala-lang.org/typeness/spray-json-refined), [Play](https://github.com/kwark/play-refined), and [argonaut](https://index.scala-lang.org/alexarchambault/argonaut-shapeless).

#### Database

Why stop at our HTTP endpoints? We can read and write from SQL databases using our refinement types, in this example using the [`doobie` library](https://github.com/tpolecat/doobie) and its `doobie-refined` module to implement a `find` method:

```scala
import doobie.implicits._
import doobie.refined.implicits._  // for support for refinement types
import cats.effect.IO

final case class Account(
  accountNumber: AccountNumber,
  username: Username,
  email: EmailAddress,
  score: Score
)

final class AccountRepository(transactor: Transactor[IO]) {

  def find(accountNumber: AccountNumber): IO[Option[Account]] = {
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
```

Reading and writing our entities using refinement gives confidence in the format of our persisted data, and I prefer this approach — which keeps the details and validation in our application logic — over adding constraints in the database.

#### Other `refined` Integrations

When writing microservices, I enjoy using refinement types throughout the entire application: from loading configuration, receiving requests, querying the database and publishing events. My validation code is baked-in for free, my types have descriptive names, and the code becomes much easier to reason about — no longer do I look at a `String` and wonder, “has it been validated at this stage of the control flow? Do we permit unpadded account numbers? Can this comment field be an empty string?” — the answer is right there in the type definition.

My examples above covered JSON and SQL examples, but these are just scratching the surface of the available `refined` support in the Scala ecosystem:

* **JSON**: [spray-json](https://index.scala-lang.org/typeness/spray-json-refined), [Play](https://github.com/kwark/play-refined), [argonaut](https://index.scala-lang.org/alexarchambault/argonaut-shapeless)
* **SQL**: [doobie](https://github.com/tpolecat/doobie), [slick](https://github.com/kwark/slick-refined)
* **Configuration**: [pureconfig](https://github.com/fthomas/refined/tree/master/modules/pureconfig/shared/src), [ciris](https://github.com/vlovgr/ciris/tree/master/modules/refined/src)
* **Avro**: [avro4s](https://github.com/sksamuel/avro4s/tree/master/avro4s-refined/src), [vulcan](https://github.com/fd4s/vulcan/tree/master/modules/refined/src)
* **Miscellaneous**: [ScalaCheck](https://github.com/fthomas/refined/tree/master/modules/scalacheck/shared/src), [Cats typeclasses](https://github.com/fthomas/refined/tree/master/modules/cats/shared/src), shapeless

(the [`refined` readme](https://github.com/fthomas/refined) describes a fuller list of support libraries)

## Refinement Types Don't Replace Data Modelling

Refinement types aren’t a panacea, and they do not replace data modelling. An objective of data modelling is to *make illegal states unrepresentable*, and refinement types aren’t able to achieve this with the same compile-time safety as [algebraic data types](https://en.wikipedia.org/wiki/Algebraic_data_type) (ADTs). Consider an example where we want to represent the time of day as “day” or “night”:

```scala
// refinement type implementation
type TimeOfDayRefined = String Refined (Equal["Day"] Or Equal["Night"])
object TimeOfDayRefined extends RefinedTypeOps[TimeOfDayRefined, String]

def whatTimeIsIt(time: TimeOfDayRefined): Unit = time match {
  case Refined("Day") => println("It's day time")
  case Refined("Night") => println("It's night time")
  // non-exhaustive match warning - the compiler can't reason about our predicate!
}

// algebraic data type implementation
sealed trait TimeOfDayAdt
case object Day extends TimeOfDayAdt
case object Night extends TimeOfDayAdt

def whatTimeIsIt(time: TimeOfDayAdt): Unit = time match {
  case Day => println("It's day time")
  case Night => println("It's night time")
  // this is exhaustive - our compiler is happy!
}
```

With the `refined` implementation, we cannot satisfy the compiler’s exhaustivity check with our predicate like we can using an ADT. Although we can reassure ourselves that the code is correct for now, we are vulnerable to future refactors that change the type definition. This isn’t a theoretical problem — in a large codebase, it’s just a matter of time until a change causes something to break!

A similar problem arises when comparing a `List Refined NonEmpty` to Cats’ `final case class NonEmptyList[+A](head: A, tail: List[A])` — we can safely get the non-optional `head` of the `NonEmptyList`, but we are forced to do an unsafe `.head.get` to get the head of our refined `List`.

The best approach is inevitably situational: refinement types are well suited for data which passes through our application without being destructured or matched upon to drive business logic, as in many CRUD situations. When you find yourself needing to perform unsafe operations on refined data, strongly consider “upgrading” to an ADT.

# Summary 

* Refinement types are simply a type, `T`, that pass a predicate test `P`
* `refined` is the go-to Scala library for refinement types
* Using `refined` gives us free validation, descriptive type aliases, and leaves less room for bugs
* There is an excellent ecosystem of `refined` interoperability libraries we can use to push validation right to the edge of our applications and drastically reduce validation boilerplate
* When defining a refinement type, declare a companion object that `extends RefinedTypeOps[MyRefinedType, BaseType]` for some helpful methods, including a runtime smart constructor (`from`) and a compile time constructor (`apply`)
