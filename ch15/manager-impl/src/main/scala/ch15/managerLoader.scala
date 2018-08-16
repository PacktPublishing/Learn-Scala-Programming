package ch15

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


abstract class ManagerApplication(context: LagomApplicationContext)
    extends LagomApplication(context) with LagomKafkaClientComponents {
  lazy val boyService: BoyService = serviceClient.implement[BoyService]
  lazy val chefService: ChefService = serviceClient.implement[ChefService]
  lazy val cookService: CookService = serviceClient.implement[CookService]
  lazy val bakerService: BakerService = serviceClient.implement[BakerService]
  override lazy val lagomServer: LagomServer =
    serverFor[ManagerService](wire[ManagerServiceImpl])
}

class ManagerLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ManagerApplication(context) with AhcWSComponents {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ManagerApplication(context) with AhcWSComponents
    with LagomDevModeComponents

}
