organization := "packt"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.0-M5"

lazy val ch08 = RootProject(file("../ch08"))

lazy val ch09 = RootProject(file("../ch09"))

lazy val ch10 = project.in(file(".")).settings(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" withSources() withJavadoc(),
  addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.8")
).dependsOn(ch08, ch09)

