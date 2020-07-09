import eu.timepit.refined.api.Refined
import eu.timepit.refined.types.char.{LowerCaseChar, UpperCaseChar}
import eu.timepit.refined.types.numeric.{NonNaNDouble, NonNegBigDecimal, NonNegDouble, NonNegInt, PosBigDecimal, PosDouble, PosInt}
import eu.timepit.refined.types.string.{HexString, NonEmptyString, TrimmedString}
import eu.timepit.refined.auto._

object RefinementExplicitSmartConstructor {
  val i1: PosInt = 1
  val i2: PosInt = 2
  val i3: PosInt  = 3
  val i4: PosInt = 4
  val i5: PosInt = 5
  val i6: NonNegInt = 6
  val i7: NonNegInt = 7
  val i8: NonNegInt = 8
  val i9: NonNegInt = 9
  val i10: NonNegInt  = 10

  val d1: PosDouble = 1.1D
  val d2: PosDouble = 2.2D
  val d3: PosDouble = 3.3D
  val d4: PosDouble = 4.4D
  val d5: NonNegDouble = 5.5D
  val d6: NonNegDouble = 6.6D
  val d7: NonNegDouble = 7.7D
  val d8: NonNaNDouble = 8.8D
  val d9: NonNaNDouble = 9.9D
  val d10: NonNaNDouble = 10.1D

  val bd1: PosBigDecimal = BigDecimal(1)
  val bd2: PosBigDecimal = BigDecimal(2)
  val bd3: PosBigDecimal = BigDecimal(3)
  val bd4: PosBigDecimal = BigDecimal(4)
  val bd5: PosBigDecimal = BigDecimal(5)
  val bd6: NonNegBigDecimal = BigDecimal(6)
  val bd7: NonNegBigDecimal = BigDecimal(7)
  val bd8: NonNegBigDecimal = BigDecimal(8)
  val bd9: NonNegBigDecimal = BigDecimal(9)
  val bd10: NonNegBigDecimal = BigDecimal(10)

  val s1: NonEmptyString = "1"
  val s2: NonEmptyString = "2"
  val s3: NonEmptyString = "3"
  val s4: NonEmptyString = "4"
  val s5: HexString = "5"
  val s6: HexString = "6"
  val s7: HexString = "7"
  val s8: TrimmedString = "8"
  val s9: TrimmedString = "9"
  val s10: TrimmedString = "10"

  val c1: LowerCaseChar = 'a'
  val c2: LowerCaseChar = 'b'
  val c3: LowerCaseChar = 'c'
  val c4: LowerCaseChar = 'd'
  val c5: LowerCaseChar = 'e'
  val c6: UpperCaseChar = 'F'
  val c7: UpperCaseChar = 'G'
  val c8: UpperCaseChar = 'H'
  val c9: UpperCaseChar = 'I'
  val c10: UpperCaseChar = 'J'
}