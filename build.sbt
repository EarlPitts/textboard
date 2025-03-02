ThisBuild / organization := "textboard"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "textboard"
  )
  .aggregate(core, tests)

lazy val core = (project in file("modules/core")).settings(
  name := "core",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-effect" % "3.5.3",
    "org.typelevel" %% "cats-effect-kernel" % "3.5.3",
    "org.typelevel" %% "cats-effect-std" % "3.5.3",
    "org.tpolecat" %% "skunk-core" % "0.6.4"
  )
)

lazy val tests = (project in file("modules/tests"))
  .configs(Test)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      // "org.typelevel" %% "cats-core" % "2.9.0",
      // "org.typelevel" %% "cats-effect" % "3.5.3",
      // "org.typelevel" %% "cats-effect-kernel" % "3.5.3",
      // "org.typelevel" %% "cats-effect-std" % "3.5.3",
      "com.disneystreaming" %% "weaver-cats" % "0.8.4"
    ),
    testFrameworks += new TestFramework("weaver.framework.CatsEffect")
  )
  .dependsOn(core)
