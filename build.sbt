ThisBuild / organization := "textboard"
ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "textboard"
  )
  .aggregate(core, tests)

lazy val http4sVersion = "0.23.30"
lazy val circeVersion = "0.14.10"
lazy val doobieVersion = "1.0.0-RC7"

lazy val circe = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

lazy val doobie = Seq(
  "org.tpolecat" %% "doobie-core"     % doobieVersion,
  "org.tpolecat" %% "doobie-weaver"   % doobieVersion,
)

lazy val core = (project in file("modules/core")).settings(
  name := "core",
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core" % "2.9.0",
    "org.typelevel" %% "cats-effect" % "3.5.7",
    "org.typelevel" %% "cats-effect-kernel" % "3.5.7",
    "org.typelevel" %% "cats-effect-std" % "3.5.7",
    "org.http4s" %% "http4s-ember-client" % http4sVersion,
    "org.http4s" %% "http4s-ember-server" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.typelevel" %% "log4cats-slf4j" % "2.7.0",
    "ch.qos.logback" % "logback-classic" % "1.2.11",
    "org.xerial" % "sqlite-jdbc" % "3.49.1.0",
  ) ++ circe ++ doobie
  
)

lazy val tests = (project in file("modules/tests"))
  .configs(Test)
  .settings(
    name := "tests",
    libraryDependencies ++= Seq(
      "com.disneystreaming" %% "weaver-cats" % "0.8.4" % Test,
      "org.typelevel" %% "cats-effect-testkit" % "3.5.7" % Test,
      "org.scalacheck" %% "scalacheck" % "1.18.1" % "test",
      "com.disneystreaming" %% "weaver-scalacheck" % "0.8.4" % Test,
    )
  )
  .dependsOn(core)
