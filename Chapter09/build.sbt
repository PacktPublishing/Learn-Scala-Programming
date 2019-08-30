ThisBuild / organization := "packt"
ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.0"

lazy val ch07 = RootProject(file("../Chapter07"))

lazy val ch08 = RootProject(file("../Chapter08"))

lazy val ch09 = project
  .in(file("."))
  .settings(
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" withSources () withJavadoc (),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")
  )
  .dependsOn(ch07, ch08)
