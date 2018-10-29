package ch15

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents

abstract class BoyApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with AhcWSComponents {
  lazy val shopService: ShopService = serviceClient.implement[ShopService]
  override lazy val lagomServer: LagomServer = serverFor[BoyService](wire[BoyServiceImpl])
}

class BoyLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext) =
    new BoyApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext) =
    new BoyApplication(context) with LagomDevModeComponents

}
