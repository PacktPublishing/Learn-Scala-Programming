name := "akka-streams-bakery"

version := "1.0"

scalaVersion := "2.12.7" // 2.13.0-M5

lazy val akkaVersion = "2.5.17"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

parallelExecution in Test := false
