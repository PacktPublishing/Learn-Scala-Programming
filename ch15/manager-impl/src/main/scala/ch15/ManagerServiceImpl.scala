package ch15

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import ch15.model._
import com.lightbend.lagom.scaladsl.api._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class ManagerServiceImpl(
    boyService: BoyService,
    chefService: ChefService,
    cookService: CookService,
    bakerService: BakerService,
    as: ActorSystem)
    extends ManagerService {

  private var count: Int = 0

  private val logger = Logger("Manager")

  private implicit lazy val ec: ExecutionContext = as.dispatcher
  private implicit lazy val am: ActorMaterializer = ActorMaterializer()(as)

  chefService.resultsTopic.subscribe.atLeastOnce(chefFlow)

  override def bake(count: Int): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    val sl = shoppingList(count)
    logger.info(s"Shopping list: $sl")
    for {
      groceries <- boyService.shop.invoke(sl)
      done <- chefService.mix.invoke(groceries)
    } yield {
      logger.info(s"Sent $groceries to Chef")
      done
    }
  }

  override def sell(cnt: Int): ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    if (cnt > count) {
      Future.failed(
        new IllegalStateException(s"Only $count cookies are available"))
    } else {
      count = count - cnt
      Future.successful(cnt)
    }
  }

  override def report: ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(count)
  }

  private def shoppingList(count: Int): ShoppingList =
    ShoppingList(count, count * 30, count * 10, count * 5)

  private def chefFlow = {
    val flow: Flow[Dough, Source[ReadyCookies, NotUsed], NotUsed] = Flow[Dough]
      .map { dough =>
        logger.info(s"Dough: $dough")
        cookService.cook.invoke(dough)
      }
      .map(Source.fromFuture)
      .mapAsync(1)(bakerService.bake.invoke)

    flow.flatMapConcat { cookiesSource =>
      logger.info(s"cookiesSource $cookiesSource")
      cookiesSource.map { cookies =>
        logger.info(s"Got baked cookies, now there are $cookies available")
        count = count + cookies.count
        Done
      }.filter(_ => false).concat(Source.single(Done))
    }
  }

}
