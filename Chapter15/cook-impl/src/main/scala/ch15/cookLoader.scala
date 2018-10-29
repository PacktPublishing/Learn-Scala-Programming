package ch15

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


abstract class CookApplication(context: LagomApplicationContext)
  extends LagomApplication(context) {
  override lazy val lagomServer: LagomServer = serverFor[CookService](wire[CookServiceImpl])
}

class CookLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new CookApplication(context) with AhcWSComponents {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new CookApplication(context) with AhcWSComponents with LagomDevModeComponents

}
