package ch12

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.receptionist.Receptionist._
import ch12.Bakery.Groceries
import ch12.Manager.ReceiveGroceries
import ch12.Shop.seller
import com.typesafe.config.ConfigFactory

object Store extends App {
  val config = ConfigFactory.load("grocery.conf")
  val system = ActorSystem(seller(Shop.systemReceptionist), "Typed-Bakery", config)
}
object Shop {
  final case class ShoppingList(eggs: Int,
                                flour: Int,
                                sugar: Int,
                                chocolate: Int)
  final case class SellByList(list: ShoppingList,
                              toWhom: ActorRef[Manager.Command])

  val SellerKey = ServiceKey[SellByList]("GrocerySeller")

  type ReceptionistFactory = ActorContext[SellByList] => ActorRef[Receptionist.Command]

  val systemReceptionist: ReceptionistFactory = _.system.receptionist

  def seller(receptionist: ReceptionistFactory): Behavior[SellByList] = Behaviors.setup { ctx ⇒
    receptionist(ctx) ! Register(SellerKey, ctx.self)
    Behaviors.receiveMessage[SellByList] {
      case SellByList(list, toWhom) ⇒
        import list._
        toWhom ! ReceiveGroceries(Groceries(eggs, flour, sugar, chocolate))
        Behaviors.same
    }
  }

}
