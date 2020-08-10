import eu.timepit.refined.api.{Refined, RefinedTypeOps}
import eu.timepit.refined.boolean.Or
import eu.timepit.refined.generic.Equal

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
