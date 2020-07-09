import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{NonNaN, NonNegative, Positive}
import eu.timepit.refined.refineV
import eu.timepit.refined.char.{LowerCase, UpperCase}
import eu.timepit.refined.collection.NonEmpty
import eu.timepit.refined.string.{Trimmed, HexStringSpec}
import eu.timepit.refined.types.numeric.{NonNaNDouble, NonNegBigDecimal, NonNegDouble, NonNegInt, PosBigDecimal, PosDouble, PosInt}
import eu.timepit.refined.types.string.{HexString, NonEmptyString, TrimmedString}

object RefinementExplicitSmartConstructor {
  val i1 = refineV[Positive](1)
  val i2 = refineV[Positive](2)
  val i3 = refineV[Positive](3)
  val i4 = refineV[Positive](4)
  val i5 = refineV[Positive](5)
  val i6 = refineV[NonNegative](6)
  val i7 = refineV[NonNegative](7)
  val i8 = refineV[NonNegative](8)
  val i9 = refineV[NonNegative](9)
  val i10  = refineV[NonNegative](10)

  val d1 = refineV[Positive](1.1D)
  val d2 = refineV[Positive](2.2D)
  val d3 = refineV[Positive](3.3D)
  val d4 = refineV[Positive](4.4D)
  val d5 = refineV[NonNegative](5.5D)
  val d6 = refineV[NonNegative](6.6D)
  val d7 = refineV[NonNegative](7.7D)
  val d8 = refineV[NonNaN](8.8D)
  val d9 = refineV[NonNaN](9.9D)
  val d10 = refineV[NonNaN](10.1D)

  val bd1 = refineV[Positive](BigDecimal(1))
  val bd2 = refineV[Positive](BigDecimal(2))
  val bd3 = refineV[Positive](BigDecimal(3))
  val bd4 = refineV[Positive](BigDecimal(4))
  val bd5 = refineV[Positive](BigDecimal(5))
  val bd6 = refineV[NonNegative](BigDecimal(6))
  val bd7 = refineV[NonNegative](BigDecimal(7))
  val bd8 = refineV[NonNegative](BigDecimal(8))
  val bd9 = refineV[NonNegative](BigDecimal(9))
  val bd10 = refineV[NonNegative](BigDecimal(10))

  val s1 = refineV[NonEmpty]("1")
  val s2 = refineV[NonEmpty]("2")
  val s3 = refineV[NonEmpty]("3")
  val s4 = refineV[NonEmpty]("4")
  val s5 = refineV[HexStringSpec]("5")
  val s6 = refineV[HexStringSpec]("6")
  val s7 = refineV[HexStringSpec]("7")
  val s8 = refineV[Trimmed]("8")
  val s9 = refineV[Trimmed]("9")
  val s10 = refineV[Trimmed]("10")

  val c1 = refineV[LowerCase]('a')
  val c2 = refineV[LowerCase]('b')
  val c3 = refineV[LowerCase]('c')
  val c4 = refineV[LowerCase]('d')
  val c5 = refineV[LowerCase]('e')
  val c6 = refineV[UpperCase]('F')
  val c7 = refineV[UpperCase]('G')
  val c8 = refineV[UpperCase]('H')
  val c9 = refineV[UpperCase]('I')
  val c10 = refineV[UpperCase]('J')
}