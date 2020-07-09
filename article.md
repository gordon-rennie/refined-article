# Safer Code with Refinement Types

We devote a significant amount of time and attention as developers to ensuring that the data we receive and process is valid so that our programs may have correct behaviour. We might enforce that usernames fall within a character limit, reject malformed URLs, guard against paying negative values, or countless more domain-specific cases. In these examples, the type system could not eliminate the errors: we may have written code accepting the parameter `username: String`, but we probably never had any intention of allowing the empty string, strange Unicode glyphs, or strings thousands of characters in length.

In this blog post I will explore _refinement types_ and Scala's `refined` library[^1] implementation. Refinement types are a powerful tool for restricting the values passed into our logic, helping ensure we fail fast and gracefully on invalid values. I'd particularly like to highlight the excellent interoperability of `refined` with the wider ecosystem, and how this can make our programs safer and easier to reason about for very little effort. 

## Conventional Validation

We are familiar with the notion of guards in our logic to defend against bad inputs. In Java, the convention is to carefully specify a method's contract in its Javadoc, and throw an `IllegalArgumentException` if any inputs are invalid. In functional Scala, we prefer to return our result wrapped in an `Option` or `Either`, as used in the *smart constructor* pattern: 

```scala
final case class User private(username: String)

object User {
  def make(username: String): Either[String, User] =
    if (username.isEmpty)
      Left("Username cannot be empty")
    else if (username.length > 20)
      Left("Username must be 20 characters or less")
    else if (!username.forall(_.isLetterOrDigit))
      Left("Usernames can only contain alphanumeric characters")
    else
      Right(User(username))
}

$ User.make("xX_ŚePhI®OtH_Xx")
$ val res0: Either[String,User] = Left(Usernames can only contain alphanumeric characters)

$ User.make("Luna")
$ val res1: Either[String,User] = Right(User(Luna))
```

There are a couple of drawbacks with this approach:

* the `user.username` field remains typed as a `String`; we've "thrown away" the potentially useful information that it's an alphanumeric string of one to 20 characters
* we've had to write and test the validation code, and will have plenty more similar code to write as our domain grows

Lets see if we can improve the situation; enter refinement types!

## Refinement Types

> In type theory, a refinement type is a type endowed with a predicate which is assumed to hold for any element of the refined type.
>
> &mdash; the original Haskell `refined` library [^2]

The building blocks are simple: 

* given some type `T` 
* and a predicate test `P`
* then our refinement type is `T Refined P`, and we know that `P` is true for all of its values

Scala's `refined` library provides this functionality, and has a large number of built-in predicate types that we can use for our `P`. A commonly used refinement type provided by `refined` is for a non-empty string (`type NonEmptyString = String Refined NonEmpty`), which comes with its own smart constructor:

```scala
import eu.timepit.refined.types.string.NonEmptyString

$ NonEmptyString.from("")
$ val res0: Either[String,NonEmptyString] = Left(Predicate isEmpty() did not fail.)

$ NonEmptyString.from("hello")
$ val res1: Either[String,NonEmptyString] = Right(hello)
```

The predicates we have to work with out-of-the-box include:

* boolean: `Not[P]`, `Or[P1, P2]`, `And[P1, P2]`, `AllOf[Ps]`, `AnyOf[Ps]`
* numeric: `Greater[x]`, `LessEqual[x]`,  `interval.OpenClosed[xMin, xMax]`
* collections: `MinSize[x]`, `Forall[P]`, `Exists[P]`
* strings: `EndsWith[s]`, `MatchesRegex[s]`, `Url`, `ValidFloat`

(The full selection can be found in the documentation[^1])

From this rich set of predicates, some of which operate on or combine other predicates, we can express diverse constraints. Let's revisit our `username` example:

```scala
type Username = String Refined And[
  Forall[LetterOrDigit],  // all chars must meet sub-predicate `LetterOrDigit`
  Size[OpenClosed[0, 20]] // size must be gt 0, leq 20
]

object Username extends RefinedTypeOps[Username, String]  // "free" smart constructor amd more!

$ Username.from("Bob")
$ val res2: Either[String,Username] = Right(Bob)

$ Username.from("")
$ val res3: Either[String,Username] = Left(Right predicate of (() && ((0 > 0) && !(0 > 20))) failed: Predicate taking size() = 0 failed: Left predicate of ((0 > 0) && !(0 > 20)) failed: Predicate failed: (0 > 0).)
```

// TODO: benefits? Performance considerations, validation for free, not losing information

The error messages are clearly quite robotic! They are generally useful for developers, and can be adapted to be more pleasant for end-users when needed.

Let's look at some more examples, and how our refinement types can be used with other libraries.

## `refined` In Action

### Compile Time Refinement

`refined` has a super power: we can infer literals as refinement types at compile time without having to go via the smart constructor, or worry about handling the error channel. The literals are tested against the predicate, and failures will emit a compiler error. This functionality is helpful when defining hard-coded configuration values or test data.

```scala
import eu.timeput.refined.auto._

$ val x: PosInt = 3  // alias for `Int Refined Positive`
$ val x: PosInt = 3

$ val y: String Refined Url = "htp://example.com"
$ Error:(8, 29) Url predicate failed: unknown protocol: htp

$ val z: NonNegBigDecimal = BigDecimal(2.2)
$ val z: NonNegBigDecimal = 2
```

Note that we had to import `eu.timeput.refined.auto._` to enable this behaviour, and explicitly type our variables, *e.g.* `val x: PosInt` — otherwise the compiler would infer `x` as the base type, `Int`.

### Runtime Refinement



## Manual Validation

from a type with raw Strings and Ints to a type with refined fields, showing error reporting benefits

## Library Integration

explain required integration library imports

### JSON Parsing

JSON example with error reporting

### Configuration Loading

ciris example?

### Other Integrations

List integration libraries

## Refined Types Don't Replace Data Modelling

give example of refined NEList vs. cats NEList, or maybe an email example.



**AOB**

- the `.unsafeFrom` escape hatch



[^1]: https://github.com/fthomas/refined
[^2]:https://github.com/nikita-volkov/refined