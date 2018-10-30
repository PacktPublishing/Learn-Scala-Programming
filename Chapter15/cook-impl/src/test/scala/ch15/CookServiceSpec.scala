package ch15

import ch15.model.Dough
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, Matchers}
import play.api.libs.ws.ahc.AhcWSComponents

class CookServiceSpec extends AsyncWordSpec with Matchers {

  "The CookService" should {
    "make cookies from Dough" in ServiceTest.withServer(ServiceTest.defaultSetup) { ctx =>
      new CookApplication(ctx) with LocalServiceLocator with AhcWSComponents
    } { server =>
      val client = server.serviceClient.implement[CookService]

      client.cook.invoke(Dough(200)).map { cookies =>
        cookies.count should ===(3)
      }
    }
  }
}
