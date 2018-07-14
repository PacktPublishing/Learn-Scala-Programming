package ch12

import akka.actor.typed.receptionist.Receptionist.Register
import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import ch12.Manager.ReceiveGroceries
import ch12.Shop.{SellByList, SellerKey}

object Bakery extends App {
  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)
  final case class Pastry(weight: Int)
  final case class RawCookies(count: Int)
  final case class ReadyCookies(count: Int)

  val seller: Behavior[SellByList] = Behaviors.setup { ctx ⇒
    ctx.system.receptionist ! Register(SellerKey, ctx.self)
    Behaviors.receiveMessage[SellByList] {
      case SellByList(list, toWhom) ⇒
        import list._
        toWhom ! ReceiveGroceries(Groceries(eggs, flour, sugar, chocolate))
        Behaviors.same
    }
  }

  val system = ActorSystem(Manager.openBakery, "Typed-Bakery")

}
