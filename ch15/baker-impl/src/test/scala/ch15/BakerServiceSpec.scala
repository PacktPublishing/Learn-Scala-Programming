package ch15

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import ch15.model.{RawCookies, ReadyCookies}
import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.{AsyncWordSpec, Matchers}

class BakerServiceSpec extends AsyncWordSpec with Matchers {

  "The BakerService" should {
    "bake cookies" in ServiceTest.withServer(ServiceTest.defaultSetup) { ctx =>
      new BakerApplication(ctx) with LocalServiceLocator
    } { server =>
      implicit val as: Materializer = server.materializer
      val input: Source[RawCookies, NotUsed] =
        Source(List(RawCookies(10), RawCookies(10), RawCookies(10)))
          .concat(Source.maybe)

      val client = server.serviceClient.implement[BakerService]

      client.bake.invoke(input).map { output =>
        val probe = output.runWith(TestSink.probe(server.actorSystem))
        probe.request(10)
        probe.expectNext(ReadyCookies(12))
        probe.expectNext(ReadyCookies(12))
        // because the oven is not full for the 6 other
        probe.cancel
        succeed
      }
    }
  }
}
