package ch13

import akka.actor._
import Bakery.Groceries
import com.typesafe.config.ConfigFactory

object Store extends App {
  final case class ShoppingList(eggs: Int,
                                flour: Int,
                                sugar: Int,
                                chocolate: Int)
  lazy val store = ActorSystem("Store", ConfigFactory.load("grocery.conf"))

  val seller: ActorRef = store.actorOf(Props(new Actor {
    override def receive: Receive = {
      case s: ShoppingList =>
        ShoppingList.unapply(s).map(Groceries.tupled).foreach(sender() ! _)
    }
  }), "Seller")
}
