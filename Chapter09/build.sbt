ThisBuild / organization := "packt"
ThisBuild / version := "1.0-SNAPSHOT"
ThisBuild / scalaVersion := "2.13.0-M5"

lazy val ch07 = RootProject(file("../ch07"))

lazy val ch08 = RootProject(file("../ch08"))

lazy val ch09 = project.in(file(".")).settings(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" withSources() withJavadoc(),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
).dependsOn(ch07, ch08)



