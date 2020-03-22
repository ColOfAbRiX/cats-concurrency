import Dependencies._
import AllProjectsKeys.autoImport._

lazy val ScalaLangVersion = "2.13.0"

// General
ThisBuild / organization := "com.colofabrix.scala.fibers"
ThisBuild / scalaVersion := ScalaLangVersion
scalaVersion := ScalaLangVersion

// Compiler options
// ThisBuild / scalacOptions ++= Compiler.TpolecatOptions ++ Seq("-P:splain:all")

// GIT version information
ThisBuild / dynverVTagPrefix := false

// Wartremover
ThisBuild / wartremoverExcluded ++= (baseDirectory.value * "**" / "src" / "test").get

// Scalafmt
ThisBuild / scalafmtOnCompile := true

// Global dependencies and compiler plugins
ThisBuild / libraryDependencies ++= Seq(
  BetterMonadicForPlugin,
  KindProjectorPlugin,
  PPrintDep,
  SplainPlugin,
  WartremoverPlugin,
) ++ Seq(
).flatten

//  PROJECTS  //

// Root project
lazy val fibersRoot: Project = project
  .in(file("."))
  .settings(
    name := "fibers",
    description := "Testing fibers",
    libraryDependencies ++= Seq(
      CatsBundle,
    ).flatten ++ Seq(
      ScalatestDep,
    ),
  )
  .aggregate(
  )

