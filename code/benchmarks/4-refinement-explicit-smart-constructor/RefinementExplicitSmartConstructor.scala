import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.char.{LowerCaseChar, UpperCaseChar}
import eu.timepit.refined.types.numeric.{NonNaNDouble, NonNegBigDecimal, NonNegDouble, NonNegInt, PosBigDecimal, PosDouble, PosInt}
import eu.timepit.refined.types.string.{HexString, NonEmptyString, TrimmedString}

object RefinementExplicitSmartConstructor {
  val i1 = PosInt.from(1)
  val i2 = PosInt.from(2)
  val i3  = PosInt.from(3)
  val i4 = PosInt.from(4)
  val i5 = PosInt.from(5)
  val i6 = NonNegInt.from(6)
  val i7 = NonNegInt.from(7)
  val i8 = NonNegInt.from(8)
  val i9 = NonNegInt.from(9)
  val i10  = NonNegInt.from(10)

  val d1 = PosDouble.from(1.1D)
  val d2 = PosDouble.from(2.2D)
  val d3 = PosDouble.from(3.3D)
  val d4 = PosDouble.from(4.4D)
  val d5 = NonNegDouble.from(5.5D)
  val d6 = NonNegDouble.from(6.6D)
  val d7 = NonNegDouble.from(7.7D)
  val d8 = NonNaNDouble.from(8.8D)
  val d9 = NonNaNDouble.from(9.9D)
  val d10 = NonNaNDouble.from(10.1D)

  val bd1 = PosBigDecimal.from(BigDecimal(1))
  val bd2 = PosBigDecimal.from(BigDecimal(2))
  val bd3 = PosBigDecimal.from(BigDecimal(3))
  val bd4 = PosBigDecimal.from(BigDecimal(4))
  val bd5 = PosBigDecimal.from(BigDecimal(5))
  val bd6 = NonNegBigDecimal.from(BigDecimal(6))
  val bd7 = NonNegBigDecimal.from(BigDecimal(7))
  val bd8 = NonNegBigDecimal.from(BigDecimal(8))
  val bd9 = NonNegBigDecimal.from(BigDecimal(9))
  val bd10 = NonNegBigDecimal.from(BigDecimal(10))

  val s1 = NonEmptyString.from("1")
  val s2 = NonEmptyString.from("2")
  val s3 = NonEmptyString.from("3")
  val s4 = NonEmptyString.from("4")
  val s5 = HexString.from("5")
  val s6 = HexString.from("6")
  val s7 = HexString.from("7")
  val s8 = TrimmedString.from("8")
  val s9 = TrimmedString.from("9")
  val s10 = TrimmedString.from("10")

  val c1 = LowerCaseChar.from('a')
  val c2 = LowerCaseChar.from('b')
  val c3 = LowerCaseChar.from('c')
  val c4 = LowerCaseChar.from('d')
  val c5 = LowerCaseChar.from('e')
  val c6 = UpperCaseChar.from('F')
  val c7 = UpperCaseChar.from('G')
  val c8 = UpperCaseChar.from('H')
  val c9 = UpperCaseChar.from('I')
  val c10 = UpperCaseChar.from('J')
}