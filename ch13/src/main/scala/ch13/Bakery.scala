package ch13

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import akka.stream._
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.util.Timeout
import akka.{Done, NotUsed}
import ch13.Bakery.{Dough, Groceries, RawCookies, ReadyCookies}
import ch13.Store.ShoppingList

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Random

object Bakery extends App {

  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)

  final case class Dough(weight: Int) {
    def +(d: Dough): Dough = Dough(weight + d.weight)
  }

  final case class RawCookies(count: Int) {
    def +(c: RawCookies): RawCookies = RawCookies(count + c.count)
  }

  final case class ReadyCookies(count: Int)

  implicit val bakery: ActorSystem = ActorSystem("Bakery")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = bakery.dispatcher

  val flow = Boy.shopFlow
    .via(Chef.mixFlow)
    .via(Cook.formFlow)
    .via(Baker.bakeFlow)
    .via(Oven.bakeFlow)

  import Manager._

  val matValue = manager.via(flow).runWith(consumer)

  matValue.onComplete(_ => afterAll)

  private def afterAll = bakery.terminate()
}

object Manager {
  private def shoppingList: ShoppingList = {
    val eggs = Random.nextInt(10) + 5
    ShoppingList(eggs, eggs * 50, eggs * 10, eggs * 5)
  }

  private val delay = 1 second
  private val interval = 1 second

  val manager: Source[ShoppingList, Cancellable] =
    Source.tick(delay, interval, NotUsed).map { _ =>
      val sl = shoppingList
      println(s"New request $sl")
      sl
    }

  val consumer: Sink[ReadyCookies, Future[Done]] =
    Sink.foreach(c => println(s"$c, yummi..."))
}

object Boy {
  implicit val timeout: Timeout = 1 second

  def lookupSeller(implicit as: ActorSystem): Future[ActorRef] = {
    val store = "akka.tcp://Store@127.0.0.1:2553"
    val seller = as.actorSelection(s"$store/user/Seller")
    seller.resolveOne()
  }

  def goShopping(
      implicit as: ActorSystem,
      ec: ExecutionContext): Future[Flow[ShoppingList, Groceries, NotUsed]] =
    lookupSeller.map { ref =>
      println(s"Going shopping to ${ref.path}")
      Flow[ShoppingList].ask[Groceries](ref)
    }

  def shopFlow(implicit as: ActorSystem, ec: ExecutionContext) =
    Flow.lazyInitAsync(() => goShopping)
}

object Chef {
  private val mixTime = 5 seconds
  def mixFlow: Flow[Groceries, Dough, NotUsed] = {
    Flow[Groceries]
      .map(splitByMixer)
      .flatMapConcat(mixInParallel)
  }

  private def splitByMixer(g: Groceries) = {
    import g._
    val single = Groceries(1, flour / eggs, sugar / eggs, chocolate / eggs)
    List.fill(g.eggs)(single)
  }

  private def mixInParallel(list: List[Groceries]) = {
    Source(list)
      .via(Balancer(subMixFlow, list.size))
      .grouped(list.size)
      .map(_.reduce(_ + _))
  }

  private def subMixFlow: Flow[Groceries, Dough, NotUsed] =
    Flow[Groceries].async("mixers-dispatcher", 1).map(mix)

  private def mix(g: Groceries) = {
    Thread.sleep(mixTime.toMillis)
    import g._
    Dough(eggs * 50 + flour + sugar + chocolate)
  }
}

object Cook {
  def formFlow: Flow[Dough, RawCookies, NotUsed] =
    Flow[Dough].map { dough =>
      print(s"Forming $dough - ")
      val result = RawCookies(makeCookies(dough.weight))
      println(result)
      result
    }

  private val cookieWeight = 50
  private def makeCookies(weight: Int): Int = weight / cookieWeight

}

object Baker {
  def bakeFlow = {
    Flow[RawCookies]
      .flatMapConcat(extractFromBox)
      .grouped(12)
      .map(_.reduce(_ + _))
  }

  private def extractFromBox(c: RawCookies) = {
    Source(List.fill(c.count)(RawCookies(1)))
  }
}

object Oven {
  private val ovenSize = 12
  private val bakingTime = 2 seconds

  def bakeFlow: Flow[RawCookies, ReadyCookies, NotUsed] =
    Flow[RawCookies]
      .delay(bakingTime, DelayOverflowStrategy.backpressure)
      .addAttributes(Attributes.inputBuffer(0, 1))
      .map(bake)

  private def bake(c: RawCookies): ReadyCookies = {
    print(s"Baking $c ... ")
    assert(c.count == ovenSize)
    Thread.sleep(bakingTime.toMillis)
    println(s"done!")
    ReadyCookies(c.count)
  }
}
