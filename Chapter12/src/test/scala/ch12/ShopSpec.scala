package ch12

import akka.actor.testkit.typed.Effect.NoEffects
import akka.actor.testkit.typed.scaladsl.{BehaviorTestKit, TestInbox}
import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.receptionist.Receptionist.Register
import ch12.Bakery.Groceries
import ch12.Manager.ReceiveGroceries
import ch12.Shop.{SellByList, ShoppingList}
import org.scalatest.WordSpec

import scala.language.postfixOps

class ShopSpec extends WordSpec {

  "A seller in the shop" should {
    "return groceries if given a shopping list" in {
      val receptionist = TestInbox[Receptionist.Command]()
      val mockReceptionist: Shop.ReceptionistFactory = _ => receptionist.ref
      val seller = BehaviorTestKit(Shop.seller(mockReceptionist))
      val inbox = TestInbox[Manager.Command]()
      val message = ShoppingList(1,1,1,1)
      seller.run(SellByList(message, inbox.ref))
      inbox.expectMessage(ReceiveGroceries(Groceries(1, 1, 1, 1)))
      receptionist.expectMessage(Register(Shop.SellerKey, seller.ref))
      seller.expectEffect(NoEffects)
    }
  }
}
