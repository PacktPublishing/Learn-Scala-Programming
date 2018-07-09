package ch11

import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import ch11.Cook.RawCookies
import ch11.Manager.ShoppingList
import ch11.Oven.Cookies
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class BakerySpec(_system: ActorSystem)
    extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with ImplicitSender {

  def this() = this(ActorSystem("BakerySpec"))

  override def afterAll: Unit = shutdown(system)

  "The boy should" should {
    val boyProps = Boy.props(system.actorSelection(testActor.path))
    val boy = system.actorOf(boyProps)

    "forward given ShoppingList to the seller" in {
      val list = ShoppingList(0, 0, 0, 0)
      boy ! list
      within(3 millis, 20 millis) {
        expectMsg(list)
        lastSender shouldBe testActor
      }
    }
    "ignore other message types" in {
      boy ! 'GoHome
      expectNoMessage(500 millis)
    }
  }
  "The baker should" should {
    val parent = TestProbe()
    val baker = parent.childActorOf(Props(classOf[Baker], 0 millis))
    "bake cookies in batches" in {
      val count = Random.nextInt(100)
      baker ! RawCookies(Oven.size * count)
      parent.expectMsgAllOf(List.fill(count)(Cookies(Oven.size)):_*)
    }
  }
}
