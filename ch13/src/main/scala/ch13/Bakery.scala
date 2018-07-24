package ch13

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.stream._
import akka.stream.scaladsl.{BidiFlow, Flow, Keep, RunnableGraph, Sink, Source}
import akka.util.Timeout
import akka.{Done, NotUsed}
import ch13.Bakery.{Dough, Groceries, RawCookies, ReadyCookies}
import ch13.Store.ShoppingList

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.Random

object BakeryApp extends App {
  import Bakery._
  import Manager._

  implicit val bakery: ActorSystem = ActorSystem("Bakery")
  implicit val materializer: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = bakery.dispatcher

  val matValue: RunnableGraph[Future[Done]] = manager.via(flow).toMat(consumer)(Keep.right)

  matValue.run().onComplete(_ => afterAll)

  private def afterAll = bakery.terminate()
}

object Bakery {

  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)

  final case class Dough(weight: Int) {
    def +(d: Dough): Dough = Dough(weight + d.weight)
  }

  final case class RawCookies(count: Int) {
    def +(c: RawCookies): RawCookies = RawCookies(count + c.count)
  }

  final case class ReadyCookies(count: Int)

  private val bakerFlow: Flow[RawCookies, ReadyCookies, NotUsed] =
    Baker.bakeFlow.join(Oven.bakeFlow)

  def flow(implicit as: ActorSystem, ec: ExecutionContext)
    : Flow[ShoppingList, ReadyCookies, Future[Option[NotUsed]]] =
    Boy.shopFlow
      .via(Chef.mixFlow)
      .via(Cook.formFlow)
      .via(bakerFlow)
}

object Manager {
  private def shoppingList: ShoppingList = {
    val eggs = Random.nextInt(10) + 5
    val result = ShoppingList(eggs, eggs * 50, eggs * 10, eggs * 5)
    println(s"Shopping list $result")
    result
  }

  private val delay = 1 second
  private val interval = 1 second

  val manager: Source[ShoppingList, NotUsed] =
    Source.repeat(NotUsed).map(_ => shoppingList)

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
      Flow[ShoppingList].ask[Groceries](ref)
    }

  def shopFlow(implicit as: ActorSystem, ec: ExecutionContext)
    : Flow[ShoppingList, Groceries, Future[Option[NotUsed]]] =
    Flow.lazyInitAsync { () =>
      goShopping
    }
}

object Chef {

  object MotorOverheatException extends Exception
  object SlowRotationSpeedException extends Exception
  object StrongVibrationException extends Exception

  val exceptions = Seq(MotorOverheatException,
                       SlowRotationSpeedException,
                       StrongVibrationException)

  private val mixTime = 1 seconds
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
    Flow[Groceries]
      .async("mixers-dispatcher", 1)
      .map(mix)
      .recover {
        case MotorOverheatException => Dough(0)
      }
      .withAttributes(ActorAttributes.supervisionStrategy(strategy))

  val strategy: Supervision.Decider = {
    case StrongVibrationException ⇒ println("resuming"); Supervision.resume
    case _ => Supervision.Stop
  }

  private def mix(g: Groceries) =
    try {
      if (Random.nextBoolean())
        throw exceptions(Random.nextInt(exceptions.size))
      Thread.sleep(mixTime.toMillis)
      import g._
      Dough(eggs * 50 + flour + sugar + chocolate)
    } catch {
      case SlowRotationSpeedException =>
        Thread.sleep(mixTime.toMillis * 2)
        import g._
        Dough(eggs * 50 + flour + sugar + chocolate)
    }
}

object Cook {
  def formFlow: Flow[Dough, RawCookies, NotUsed] =
    Flow[Dough]
      .log("Cook[Before Map]")
      .map { dough =>
        RawCookies(makeCookies(dough.weight))
      }
      .log("Cook[After Map]")
      .withAttributes(
        Attributes.logLevels(
          onElement = Logging.InfoLevel,
          onFinish = Logging.DebugLevel,
          onFailure = Logging.WarningLevel
        )
      )

  private val cookieWeight = 50
  private def makeCookies(weight: Int): Int = weight / cookieWeight
}

object Baker {
  def bakeFlow = BidiFlow.fromFlows(inFlow, outFlow)

  private val inFlow = Flow[RawCookies]
    .flatMapConcat(extractFromBox)
    .grouped(Oven.ovenSize)
    .map(_.reduce(_ + _))

  private def outFlow = Flow[ReadyCookies]

  private def extractFromBox(c: RawCookies) =
    Source(List.fill(c.count)(RawCookies(1)))
}

object Oven {
  val ovenSize = 12
  private val bakingTime = 2 seconds

  def bakeFlow: Flow[RawCookies, ReadyCookies, NotUsed] =
    Flow[RawCookies]
      .delay(bakingTime, DelayOverflowStrategy.backpressure)
      .addAttributes(Attributes.inputBuffer(1, 1))
      .map(bake)

  private def bake(c: RawCookies): ReadyCookies = {
    assert(c.count == ovenSize)
    ReadyCookies(c.count)
  }
}
