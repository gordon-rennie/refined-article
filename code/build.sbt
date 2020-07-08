lazy val root = (project in file("."))
  .settings(name := "refined-article")
  .settings(
    scalaVersion := "2.13.3",
    version := "0.1",
    addCompilerPlugin(
      ("org.typelevel" %% "kind-projector" % "0.11.0").cross(CrossVersion.full)
    ),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    libraryDependencies ++= Dependencies.dependencies
  )
