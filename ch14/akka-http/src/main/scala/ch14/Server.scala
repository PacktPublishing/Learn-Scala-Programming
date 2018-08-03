package ch14

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Server extends App with Routes with JsonSupport {

  val config = Config.load()

  implicit val system: ActorSystem = ActorSystem("ch14")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  lazy val inventory: ActorRef = system.actorOf(InventoryActor.props, InventoryActor.persistenceId)

  DB.initialize(config.database)
  Http().bindAndHandle(routes, config.server.host, config.server.port)
  Await.result(system.whenTerminated, Duration.Inf)
}
