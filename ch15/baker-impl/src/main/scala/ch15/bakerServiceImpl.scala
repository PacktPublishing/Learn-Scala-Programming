package ch15

import akka.NotUsed
import akka.stream.{Attributes, DelayOverflowStrategy}
import akka.stream.scaladsl.{BidiFlow, Flow, Source}
import ch15.model._
import com.lightbend.lagom.scaladsl.api._

import scala.concurrent.duration._
import scala.concurrent.Future

import play.api.Logger

class BakerServiceImpl extends BakerService {

  private val logger = Logger("Baker")

  override def bake: ServiceCall[Source[RawCookies, NotUsed], Source[ReadyCookies, NotUsed]] = ServiceCall { dough =>
    logger.info(s"Baking: $dough")
    Future.successful(dough.via(bakerFlow))
  }

  private val bakerFlow: Flow[RawCookies, ReadyCookies, NotUsed] =
    Baker.bakeFlow.join(Oven.bakeFlow)
}

object Baker {
  private val logger = Logger("BakerFlow")

  def bakeFlow: BidiFlow[RawCookies, RawCookies, ReadyCookies, ReadyCookies, NotUsed] = BidiFlow.fromFlows(inFlow, outFlow)

  private val inFlow = Flow[RawCookies]
    .flatMapConcat(extractFromBox)
    .grouped(Oven.ovenSize)
    .map(_.reduce(_ + _))

  private def outFlow = Flow[ReadyCookies].map { c =>
    logger.info(s"Sending to manager: $c")
    c
  }

  private def extractFromBox(c: RawCookies) = {
    logger.info(s"Extracting: $c")
    Source(List.fill(c.count)(RawCookies(1)))
  }
}

object Oven {
  private val logger = Logger("Oven")

  val ovenSize = 12
  private val bakingTime = 2.seconds

  def bakeFlow: Flow[RawCookies, ReadyCookies, NotUsed] =
    Flow[RawCookies]
      .map(bake)
      .delay(bakingTime, DelayOverflowStrategy.backpressure)
      .addAttributes(Attributes.inputBuffer(1, 1))

  private def bake(c: RawCookies): ReadyCookies = {
    logger.info(s"Baked: $c")
    assert(c.count == ovenSize)
    ReadyCookies(c.count)
  }
}
