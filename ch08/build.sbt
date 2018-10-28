organization := "packt"
version := "1.0-SNAPSHOT"
scalaVersion := "2.13.0-M5"

lazy val ch07 = RootProject(file("../ch07"))

lazy val ch08 = project.in(file(".")).settings(
  scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked"),
  libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" withSources() withJavadoc(),
).dependsOn(ch07)
