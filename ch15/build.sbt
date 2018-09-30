organization in ThisBuild := "packt"
version in ThisBuild := "1.0-SNAPSHOT"

scalaVersion in ThisBuild := "2.12.7"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.1" % Provided
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5" % Test

val defaultDependencies = Seq(lagomScaladslTestKit, macwire, scalaTest)

lazy val bakery = (project in file("."))
  .aggregate(
    `shared-model`,
    `boy-api`, `boy-impl`,
    `chef-api`, `chef-impl`,
    `cook-api`, `cook-impl`,
    `baker-api`, `baker-impl`,
    `manager-api`, `manager-impl`)

lazy val `shared-model` = (project in file("shared-model"))
  .settings(libraryDependencies += lagomScaladslApi)

lazy val `boy-api` = (project in file("boy-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `chef-api` = (project in file("chef-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `cook-api` = (project in file("cook-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `baker-api` = (project in file("baker-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `manager-api` = (project in file("manager-api"))
  .settings(libraryDependencies += lagomScaladslApi)
  .dependsOn(`shared-model`)

lazy val `boy-impl` = (project in file("boy-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .dependsOn(`boy-api`)

lazy val `chef-impl` = (project in file("chef-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      lagomScaladslPubSub,
      macwire
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`chef-api`)

lazy val `cook-impl` = (project in file("cook-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .dependsOn(`cook-api`)

lazy val `baker-impl` = (project in file("baker-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .dependsOn(`baker-api`)

lazy val `manager-impl` = (project in file("manager-impl"))
  .enablePlugins(LagomScala)
  .settings(libraryDependencies ++= defaultDependencies)
  .settings(libraryDependencies += lagomScaladslKafkaBroker)
  .dependsOn(`manager-api`, `boy-api`, `chef-api`, `cook-api`, `baker-api`)

lagomUnmanagedServices in ThisBuild := Map("GroceryShop" -> "http://localhost:8080")
