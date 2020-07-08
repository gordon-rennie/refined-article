import sbt._

object Dependencies {
  object Version {
    val cats = "2.2.0-M2"
    val catsEffect = "2.1.3"
    val fs2 = "2.3.0"
    val refined = "0.9.14"
    val ciris = "1.0.4"
    val circe = "0.13.0"
  }

  object Library {
    val cats = "org.typelevel" %% "cats-core" % Version.cats
    val catsEffect = "org.typelevel" %% "cats-effect" % Version.catsEffect
    val fs2 = "co.fs2" %% "fs2-core" % Version.fs2
    val refined = "eu.timepit" %% "refined" % Version.refined
    val refinedCats = "eu.timepit" %% "refined-cats" % Version.refined

    val ciris = List("is.cir" %% "ciris", "is.cir" %% "ciris-refined")
      .map(_ % Version.ciris)
  }

  lazy val dependencies: List[ModuleID] = List(
    Library.cats,
    Library.catsEffect,
    Library.fs2,
    Library.refined,
    Library.refinedCats
  ) ++ Library.ciris
}
