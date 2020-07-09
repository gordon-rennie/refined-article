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
* and a predicate test `P`
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
  * The error messages are clearly quite robotic! They are generally sufficient for developers, and can be adapted to be more pleasant for end-users when needed.


We gained this with no runtime cost; our compiled bytecode will see only a `String`. (For refinements on value types[^3] such as `Int`, we would incur only the small cost of boxing it). 

Let's look at some more examples, and how our refinement types can be used with other libraries.

> **_NOTE:_** It might seem strange seeing literal values in your predicate types, as in `Int Refined GreaterEqual[0]`. Under the hood, the value `0` is being treated as a literal-based singleton type[^4], newly supported in Scala 2.13.
>
> If you are on an earlier version of Scala, `refined` has you use a `shapeless` implementation instead, which is simple but less readable: ``Int Refined GreaterEqual[W.`0`.T]`` – refer to the docs[^1].

## Refined In Action

### Compile Time Refinement

`refined` has a nifty super power: we can infer literals as refinement types at compile time without having to go via the smart constructor, or worry about handling the error channel. The literals are tested against the predicate, and any failures will emit an error at compile time. This functionality is helpful when defining hard-coded configuration values or test data.

```scala
import eu.timepit.refined.refineMV
import eu.timepit.refined.collection.NonEmpty

$ refineMV[NonEmpty]("foo")  // explicit refinement
| val res0: Refined[String,NonEmpty] = foo

$ NonEmptyString("bar") // alternative explicit refinement using companion object's `RefinedTypeOps`
| val res1: NonEmptyString = bar  // alias expands to Refined[String,NonEmpty]
```

We can also enable implicit auto-refinement using an import:

```scala
import eu.timepit.refined.auto._  // enables auto-refinement
import eu.timepit.refined.numeric.Positive

$ val x: Int Refined Positive = 3
| val x: Int Refined Positive = 3
$ x.value  // unwraps the value
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

We had to explicitly type our variables, *e.g.* `val x: PosInt` — otherwise the compiler would infer `x` as the base type, `Int`. We also saw that we can unwrap any refined value back to the base type using the `.value` method — we need this when we call external code.

> **_NOTE:_** We can take advantage of Scala’s postfix notation support for better readability in our types. In fact, we already have: `refined`’s type declarations of `String Refined Predicate` is the more readable postfix equivalent of `Refined[String, Predicate]`. Similarly, we could rewrite our previous example as `type Username = String Refined (Forall[LetterOrDigit] And Size[OpenClosed[0, 20]])`

### Runtime Refinement

For the majority of the code we write, the data we process is not known at compile time. Rather, it comes from the external boundaries of our services, in the form of HTTP requests or responses, events we consume, files we read, or databases we query. In these cases we must safely convert the incoming data to our refinement types, and gracefully handle invalid values. Let’s firstly look at how we can do this manually, then explore some integration libraries that can do all the heavy lifting for us.

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
[^3]:https://docs.scala-lang.org/tour/unified-types.html
[^4]:https://docs.scala-lang.org/sips/42.type.html

