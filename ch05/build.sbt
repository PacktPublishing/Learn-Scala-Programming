organization := "packt"
version := "1.0-SNAPSHOT"
scalaVersion := "2.12.6"

scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked")

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.0" withSources() withJavadoc()
