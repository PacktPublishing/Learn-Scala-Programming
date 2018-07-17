package ch12

import akka.actor.testkit.typed.Effect.{NoEffects, Spawned}
import akka.actor.testkit.typed.scaladsl._
import akka.actor.typed.DispatcherSelector
import ch12.Baker.BakeCookies
import ch12.Bakery.{Groceries, RawCookies}
import ch12.Boy.GoShopping
import ch12.Chef.Mix
import ch12.Oven.Extract
import ch12.Shop.{SellByList, ShoppingList}
import com.typesafe.config.Config
import org.scalatest._

import scala.concurrent.duration._
import scala.language.postfixOps

class BakerySpec extends WordSpec with ActorTestKit with BeforeAndAfterAll {

  override def afterAll: Unit = shutdownTestKit()

  "The boy should" should {
    "forward given ShoppingList to the seller" in {
      val testKit = BehaviorTestKit(Boy.goShopping)
      val seller = TestInbox[Shop.SellByList]()
      val manager = TestInbox[Manager.Command]()
      val list = ShoppingList(1, 1, 1, 1)
      testKit.run(GoShopping(list, seller.ref, manager.ref))
      testKit.expectEffect(NoEffects)
      seller.expectMessage(SellByList(list, manager.ref))
      assert(!testKit.isAlive)
    }
  }
  "The chef should" should {
    "create and destroy mixers as required" in {
      // the mixerFactory is needed because behaviour equals method is not implemented correctly
      // currently behaviours compared by reference
      // therefore we need to have the same behaviour instance for the test to pass
      val mixerFactory = Mixer.mix(0 seconds)
      val chef = BehaviorTestKit(Chef.idle(mixerFactory))
      val manager = TestInbox[Manager.Command]()
      val message = Mix(Groceries(1, 1, 1, 1), manager.ref)
      chef.run(message)
      chef.expectEffect(
        Spawned(mixerFactory,
                "Mixer_1",
                DispatcherSelector.fromConfig("mixers-dispatcher")))
      val expectedByMixer = Mixer.Mix(Groceries(1, 1, 1, 1), chef.ref)
      chef.childInbox("Mixer_1").expectMessage(expectedByMixer)
    }
  }

  override def config: Config = ManualTime.config
  val manualTime: ManualTime = ManualTime()

  "The baker should" should {
    "bake cookies in batches" in {
      val oven = TestProbe[Oven.Command]()
      val manager = TestProbe[Manager.Command]()
      val baker = spawn(Baker.idle(oven.ref))
      baker ! BakeCookies(RawCookies(1), manager.ref)
      oven.expectMessage(Oven.Put(1, baker))
      manualTime.expectNoMessageFor(Baker.DefaultBakingTime - 1.millisecond,
                                    oven)
      manualTime.timePasses(Baker.DefaultBakingTime)
      oven.expectMessage(Extract(baker))
    }
  }
}
