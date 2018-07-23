package ch13

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.stream.testkit.{TestPublisher, TestSubscriber}
import akka.stream.testkit.scaladsl.{TestSink, TestSource}
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestProbe
import ch13.Bakery.{Dough, Groceries, RawCookies, ReadyCookies}
import ch13.Store.ShoppingList
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class BakerySpecPlain
    extends Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  implicit val as: ActorSystem = ActorSystem("test")
  implicit val mat: Materializer = ActorMaterializer()

  "manager source" should {
    "emit shopping lists as needed" in {
      val future: Future[Seq[ShoppingList]] =
        Manager.manager.take(100).runWith(Sink.seq)
      val result: Seq[Store.ShoppingList] = Await.result(future, 1.seconds)
      assert(result.size == 100)
    }
  }

  "cook flow" should {
    "convert flow elements one-to-one" in {
      val source = Source.repeat(Dough(100)).take(1000)
      val sink = Sink.seq[RawCookies]
      val future: Future[Seq[RawCookies]] =
        source.via(Cook.formFlow).runWith(sink)
      val result: Seq[RawCookies] = Await.result(future, 1.seconds)
      assert(result.size == 1000)
      assert(result.forall(_.count == 2))
    }
  }

  override def afterAll(): Unit = {
    as.terminate()
    super.afterAll()
  }
}

class BakerySpecTestKit
    extends Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  implicit val as: ActorSystem = ActorSystem("test")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = as.dispatcher

  "the boy flow" should {
    "lookup a remote seller and communicate with it" in {
      val probe = TestProbe()
      val source = Manager.manager.take(1)
      val sink = Sink.actorRef[Groceries](probe.ref, NotUsed)
      source.via(Boy.shopFlow).runWith(sink)
      probe.expectMsgType[Groceries]
    }
  }

  override def afterAll(): Unit = {
    as.terminate()
    super.afterAll()
  }
}

class BakerySpecTestProbe
    extends Matchers
    with WordSpecLike
    with BeforeAndAfterAll {

  implicit val as: ActorSystem = ActorSystem("test")
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = as.dispatcher

  "the whole flow" should {
    "produce cookies" in {
      val testSink = TestSink.probe[ReadyCookies]
      val source = TestSource.probe[ShoppingList]
      val (publisher: TestPublisher.Probe[ShoppingList],
           subscriber: TestSubscriber.Probe[ReadyCookies]) =
        source.via(Bakery.flow).toMat(testSink)(Keep.both).run()
      subscriber.request(10)
      publisher.sendNext(ShoppingList(30, 1000, 100, 100))
      subscriber.expectNext(140.seconds, ReadyCookies(12))
      subscriber.expectNext(140.seconds, ReadyCookies(12))
    }
  }

  override def afterAll(): Unit = {
    as.terminate()
    super.afterAll()
  }
}
