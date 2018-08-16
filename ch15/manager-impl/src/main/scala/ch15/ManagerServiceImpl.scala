package ch15

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.{Done, NotUsed}
import ch15.model.{dough, _}
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future, Promise}

class ManagerServiceImpl(boyService: BoyService,
                         chefService: ChefService,
                         cookService: CookService,
                         bakerService: BakerService,
                         as: ActorSystem)
    extends ManagerService {

  private val count: AtomicInteger = new AtomicInteger(0)

  private val logger = Logger("Manager")

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
    if (cnt > count.get()) {
      Future.failed(
        new IllegalStateException(s"Only $count cookies are available"))
    } else {
      count.addAndGet(-1 * cnt)
      Future.successful(cnt)
    }
  }

  override def report: ServiceCall[NotUsed, Int] = ServiceCall { _ =>
    Future.successful(count.get())
  }

  private def shoppingList(count: Int): ShoppingList =
    ShoppingList(count, count * 30, count * 10, count * 5)

  private implicit lazy val ec: ExecutionContext = as.dispatcher
  private implicit lazy val am: ActorMaterializer = ActorMaterializer()(as)

  val sub: Future[Done] =
    chefService.resultsTopic.subscribe.atLeastOnce(chefFlow)

  private def update(cookies: ReadyCookies) = {
    logger.info(s"Got baked cookies, now there are $cookies available")
    count.addAndGet(cookies.count)
  }
  private lazy val chefFlow: Flow[Dough, Done, NotUsed] = Flow[Dough]
    .map { dough: Dough =>
      val fut = cookService.cook.invoke(dough)
      val src = Source.fromFuture(fut)
      val ready: Future[Source[ReadyCookies, NotUsed]] =
        bakerService.bake.invoke(src)
      //val ready: Future[Source[ReadyCookies, NotUsed]] = Future.successful(src.map(r => ReadyCookies(12)).take(2))
      Source.fromFutureSource(ready)
    }
    .flatMapConcat(identity)
    .map(update)
    .map(_ => Done)

  lazy val dummyflow: Flow[Dough, Done, NotUsed] = Flow[Dough]
    .map { dough =>
      logger.info(s"Dough: $dough")
      Source.fromFuture(cookService.cook.invoke(dough)).map { r =>
        logger.info(s"Done $r"); Done
      }
    }
    .flatMapConcat(identity)

}

trait ManagerCommand
final case class AddCookies(count: Int) extends ManagerCommand with ReplyType[Int]
final case class RemoveCookies(count: Int) extends ManagerCommand with ReplyType[Int]

trait ManagerEvent
final case class NumberOfCookiesChanged(count: Int) extends ManagerEvent with AggregateEvent[NumberOfCookiesChanged] {
  override def aggregateTag: AggregateEventTag[NumberOfCookiesChanged] = AggregateEventTag[NumberOfCookiesChanged]("NumberOfCookiesChanged")
}
sealed trait ManagerState {
  def cookies: Int
}
final case class MixingState(cookies: Int) extends ManagerState
