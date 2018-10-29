package ch12

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import ch12.Shop._

object Boy {
  final case class GoShopping(shoppingList: ShoppingList,
                              seller: ActorRef[SellByList],
                              manager: ActorRef[Manager.Command])

  val goShopping = Behaviors.receiveMessage[GoShopping] {
    case GoShopping(shoppingList, seller, manager) =>
      seller ! SellByList(shoppingList, manager)
      Behaviors.stopped
  }
}
