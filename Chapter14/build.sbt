

lazy val http4sVersion = "0.18.9"
lazy val doobieVersion = "0.5.3"
lazy val h2Version = "1.4.197"
lazy val flywayVersion = "5.1.4"
lazy val circeVersion = "0.9.3"
lazy val pureConfigVersion = "0.9.1"
lazy val logbackVersion = "1.2.3"
lazy val catsVersion = "1.1.0"
lazy val scalaTestVersion = "3.0.5"
lazy val scalaMockVersion = "4.1.0"
lazy val akkaHttpVersion = "10.1.3"
lazy val akkaVersion    = "2.5.14"
lazy val akkaPersistenceVersion = "3.4.0"
lazy val staminaVersion = "0.1.4"

lazy val akkaHttp = (project in file("akka-http")).
  settings(
    inThisBuild(List(
      organization    := "Packt",
      version         := "1.0-SNAPSHOT",
      scalaVersion    := "2.12.6"
    )),
    name := "akka-http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka"   %% "akka-http"              % akkaHttpVersion,
      "com.typesafe.akka"   %% "akka-http-spray-json"   % akkaHttpVersion,
      "com.typesafe.akka"   %% "akka-stream"            % akkaVersion,
      "com.typesafe.akka"   %% "akka-persistence"       % akkaVersion,
      "com.typesafe.akka"   %% "akka-persistence-query" % akkaVersion,
      "com.typesafe.akka"   %% "akka-slf4j"             % akkaVersion,

      "com.github.dnvriend" %% "akka-persistence-jdbc"  % akkaPersistenceVersion,
      "com.scalapenos"      %% "stamina-json"           % staminaVersion,
      "com.h2database"      %  "h2"                     % h2Version,
      "org.flywaydb"        %  "flyway-core"            % flywayVersion,
      "ch.qos.logback"      %  "logback-classic"        % logbackVersion,

      "com.typesafe.akka"   %% "akka-http-testkit"      % akkaHttpVersion   % Test,
      "com.typesafe.akka"   %% "akka-testkit"           % akkaVersion       % Test,
      "com.typesafe.akka"   %% "akka-stream-testkit"    % akkaVersion       % Test,
      "org.scalatest"       %% "scalatest"              % scalaTestVersion  % Test
    )
  )

lazy val http4s = (project in file("http4s-doobie"))
  .configs(IntegrationTest)
  .settings(
    inThisBuild(List(
      organization    := "Packt",
      version         := "1.0-SNAPSHOT",
      scalaVersion    := "2.12.6"
    )),
    name := "http4s-doobie",
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-blaze-server"  % http4sVersion,
      "org.http4s"            %% "http4s-circe"         % http4sVersion,
      "org.http4s"            %% "http4s-dsl"           % http4sVersion,
      "org.tpolecat"          %% "doobie-core"          % doobieVersion,
      "org.tpolecat"          %% "doobie-h2"            % doobieVersion,
      "org.tpolecat"          %% "doobie-hikari"        % doobieVersion,
      "com.h2database"        %  "h2"                   % h2Version,
      "org.flywaydb"          %  "flyway-core"          % flywayVersion,
      "io.circe"              %% "circe-generic"        % circeVersion,
      "com.github.pureconfig" %% "pureconfig"           % pureConfigVersion,
      "ch.qos.logback"        %  "logback-classic"      % logbackVersion,
      "org.typelevel"         %% "cats-core"            % catsVersion,

      "org.http4s"            %% "http4s-blaze-client"  % http4sVersion     % "it,test",
      "io.circe"              %% "circe-literal"        % circeVersion      % "it,test",
      "io.circe"              %% "circe-optics"         % circeVersion      % "it",
      "org.scalatest"         %% "scalatest"            % scalaTestVersion  % "it,test",
      "org.scalamock"         %% "scalamock"            % scalaMockVersion  % Test
    )
  )

  lazy val root = (project in file(".")).aggregate(akkaHttp, http4s)
