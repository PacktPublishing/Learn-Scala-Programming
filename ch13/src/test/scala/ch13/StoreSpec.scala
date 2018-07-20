package ch13
/*
import akka.testkit.TestKit
import ch13.Manager.ShoppingList
import ch13.Mixer.Groceries
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.language.postfixOps

class StoreSpec(store: Store) extends TestKit(store.store)
    with Matchers with WordSpecLike with BeforeAndAfterAll {

  def this() = this(new Store {})

  override def afterAll: Unit = shutdown(system)

  "A seller in store" should {
    "do nothing for all unexpected message types" in {
      store.seller ! 'UnexpectedMessage
      expectNoMessage()
    }
    "return groceries if given a shopping list" in {
      store.seller.tell(ShoppingList(1, 1, 1, 1), testActor)
      expectMsg(Groceries(1,1,1,1))
    }
  }
}*/
