# Safe, Expressive Code with Refinement Types

We devote a significant amount of time and attention as developers to ensuring that the data we receive and process is valid so that our programs may have correct behaviour. We might enforce that usernames fall within a character limit, reject malformed URLs, guard against paying negative values, or countless more domain-specific cases. In these examples, the type system could not eliminate the errors: we may accept a parameter `username: String`, but we likely have no intention of allowing the empty string, strange Unicode glyphs, or strings thousands of characters in length.

In this blog post I will explore _refinement types_ and Scala's `refined` library implementation[^1]. Refinement types are a powerful tool for restricting the values passed into our logic, helping ensure we fail fast and gracefully on invalid values. I'd particularly like to highlight the excellent interoperability of `refined` with the wider ecosystem, and how this can make our programs safer and easier to reason about for very little effort. 

## Conventional Validation

We are familiar with the notion of guards in our logic to defend against bad inputs. In Java, the convention is to carefully specify a method's contract in its Javadoc, and throw an `IllegalArgumentException` if any inputs are invalid. In functional Scala, we prefer to return our result wrapped in an `Option` or `Either`: 

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

Lets see if we can improve the situation; enter refinement types!

## Refinement Types

> In type theory, a refinement type is a type endowed with a predicate which is assumed to hold for any element of the refined type.
>
> &mdash; the original Haskell `refined` library [^2]

The building blocks are simple: 

* given some type `T` 
* and a predicate test expressed as a type `P`
* then our refinement type is `T Refined P`, and it contains only the values in `T` for which `P` is true

Scala's `refined` library provides this functionality, and has a large number of built-in predicate types that we can use for our `P`. For example, a frequently useful refinement type is for a non-empty string (`type NonEmptyString = String Refined NonEmpty`), which is predefined for us and comes with its own smart constructor:

```scala
import eu.timepit.refined.types.string.NonEmptyString

$ NonEmptyString.from("")
| val res0: Either[String,NonEmptyString] = Left(Predicate isEmpty() did not fail.)

$ NonEmptyString.from("hello")
| val res1: Either[String,NonEmptyString] = Right(hello)
```

The predicates we have to work with out-of-the-box include:

* boolean: `Not[P]`, `Or[P1, P2]`, `And[P1, P2]`, `AllOf[Ps]`, `AnyOf[Ps]`
* numeric: `Greater[x]`, `LessEqual[x]`,  `interval.OpenClosed[xMin, xMax]`
* collections: `MinSize[x]`, `Forall[P]`, `Exists[P]`
* strings: `EndsWith[s]`, `MatchesRegex[s]`, `Url`, `ValidFloat`

(The full selection can be found in the documentation[^1])

From this rich set of predicates, some of which operate on or combine other predicates, we can express diverse constraints. Let's revisit our `username` example:

```scala
import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.And
import eu.timepit.refined.char.LetterOrDigit
import eu.timepit.refined.collection.{Forall, Size}
import eu.timepit.refined.numeric.Interval.OpenClosed

type Username = String Refined And[
  Forall[LetterOrDigit],  // all chars must meet sub-predicate `LetterOrDigit`
  Size[OpenClosed[0, 20]] // size must be gt 0, leq 20
]

object Username extends RefinedTypeOps[Username, String]  // gets us a smart constructor and more!

$ Username.from("Tito")
| val res2: Either[String,Username] = Right(Tito)

$ Username.from("")
| val res3: Either[String,Username] = Left(Right predicate of (() && ((0 > 0) && !(0 > 20))) failed: Predicate taking size() = 0 failed: Left predicate of ((0 > 0) && !(0 > 20)) failed: Predicate failed: (0 > 0).)
```

What have we gained here?

* our predicates are expressed in a declarative way in the type alias definition
* the return type is our `Username` alias: we’ve retained the information that the base `String` has been validated, and have a contextually useful name
* we were given validation logic and error reporting essentially for free
  * The error messages are clearly quite robotic! They are generally sufficient for developers, and can be adapted to be more pleasant for end users when needed.


We gained this with no runtime cost; our compiled bytecode will see only a `String`. (For refinements on value types[^3] such as `Int`, we would incur only the small cost of boxing it). 

Let's look at some more examples, and how our refinement types can be used with other libraries.

> **_NOTE:_** It might seem strange seeing literal values in your predicate types, as in `Int Refined GreaterEqual[0]`. Under the hood, the value `0` is being treated as a literal-based singleton type[^4], newly supported in Scala 2.13.
>
> If you are on a pre-2.13 version of Scala,  you must use a `shapeless` implementation instead, which is slightly less readable: ``Int Refined GreaterEqual[W.`0`.T]`` — refer to the `refined`docs[^1].

## Refined In Action

### Compile Time Refinement

`refined` has a nifty super power: we can infer refinement types at compile time without having to go via the smart constructor, which would require us to handle the error channel. This feature is available for any literal, `BigDecimal` or `BigInt`. We can choose to perform the refinement explicitly:

```scala
import eu.timepit.refined.refineMV
import eu.timepit.refined.collection.NonEmpty

$ refineMV[NonEmpty]("foo")  // explicit refinement
| val res0: Refined[String,NonEmpty] = foo

$ NonEmptyString("bar") // alternative explicit refinement using companion object's `RefinedTypeOps`
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

For auto-refinement to work we had to explicitly type our variables, *e.g.* `val x: PosInt` — otherwise the compiler would infer `x` as the base type, `Int`. We also saw that we can unwrap any refined value back to the base type using the `.value` method — we need this when we call external code.

This feature is great for specifying hard-coded values in configuration or test fixtures which need refinement types.

> **_NOTE:_** We can take advantage of Scala’s postfix notation support for better readability in our types. In fact, we already have: `refined`’s type declarations of `String Refined Predicate` is equivalent to `Refined[String, Predicate]`. Similarly, we could rewrite our previous example as `type Username = String Refined (Forall[LetterOrDigit] And Size[OpenClosed[0, 20]])`

### Runtime Refinement

The majority of data we process is not known at compile time. Rather, it comes from the external boundaries of our services, in the form of HTTP requests or responses, events we consume, files we read, or databases we query. In these cases we must safely convert the incoming data to our refinement types, and gracefully handle invalid values. Let’s firstly look at how we can do this manually, then explore some integration libraries that can do all the heavy lifting for us.

## Manual Validation

Let’s define a toy domain entity using refinement types:

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

Occasionally you will find yourself needing to refine raw values yourself, for example because you are interfacing with part of a codebase that wasn’t written with refinement types, In these situations we can elegantly compose together the `Either`s returned by the smart constructor, for example:

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

This example uses `cats` extension methods to combine the validation results, either returning a valid `Account` or a list of errors. We have also `leftMap`ped the error channels to provide a more pleasant error message.

> **_NOTE:_** The usual `Either` behaviour is to fail fast on the first error. We have taken advantage of the error-accumulating semantics when using `.parMapN` on a tuple of `Either[NonEmptyList[E], A]`. If you are familiar with `Validated`, this is  equivalent to converting from the `Either` to `ValidatedNel`s and back again — a nice trick I picked up from a talk by Gabriel Volpe[^5].

## Library Integration

Manual refinement is OK, but I’ve found the greatest benefits in using refinement types when I can push validation right to the edges of my application and get integration libraries to do the heavy lifting for me. Let’s look at some examples.

### JSON Parsing

Consider an example where we want to decode JSON requests, in this case using Circe JSON library. Taking advantage of Circe’s generic codec derivation, we can already get codecs for requests of base types:

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

The cause of the error is that Circe doesn’t have the required typeclass instances for our refinement types to do its job. This is easily fixed: Circe has a `circe-refined` module, which will take any codec for a base type (such as `Decoder[Int]`) and implicitly transform it to a refinement type codec (such as `Decoder[PosInt]`). Any refinement failures are absorbed into the usual Circe parsing error:

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

We used `circe` in our example, but integration libraries are available for [spray-json](https://index.scala-lang.org/typeness/spray-json-refined), [Play](https://github.com/kwark/play-refined), [argonaut](https://index.scala-lang.org/alexarchambault/argonaut-shapeless) and more.

### Configuration Loading

ciris example?

### Other Integrations

List integration libraries

## Refinement Types Don't Replace Data Modelling

give example of refined NEList vs. cats NEList, or maybe an email example.



**AOB**

- the `.unsafeFrom` escape hatch



[^1]: https://github.com/fthomas/refined
[^2]:https://github.com/nikita-volkov/refined
[^3]:https://docs.scala-lang.org/tour/unified-types.html
[^4]:https://docs.scala-lang.org/sips/42.type.html

[^5]:"Why types matter” talk at Scala Love 2020: https://www.youtube.com/watch?v=n1Y2V4zCZdQ