package ch14

import cats.effect.IO
import fs2.{Stream, StreamApp}
import fs2.StreamApp.ExitCode
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] with Http4sDsl[IO] {
  override def stream(args: List[String],
             requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    val config = Config.load("application.conf")
    createServer(config).flatMap(_.serve)
  }

  def createServer(config: IO[Config]): Stream[IO, BlazeBuilder[IO]] = {
    for {
      config <- Stream.eval(config)
      transactor <- Stream.eval(DB.transactor(config.database))
      _ <- Stream.eval(DB.initialize(transactor))
    } yield BlazeBuilder[IO]
      .bindHttp(config.server.port, config.server.host)
      .mountService(new Service(new Repository(transactor)).service, "/")

  }
}
