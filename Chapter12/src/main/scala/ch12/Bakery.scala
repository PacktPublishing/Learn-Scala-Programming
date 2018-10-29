package ch12

import akka.actor.typed.ActorSystem

object Bakery extends App {
  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)
  final case class Dough(weight: Int)
  final case class RawCookies(count: Int)
  final case class ReadyCookies(count: Int)

  val system = ActorSystem(Manager.openBakery, "Typed-Bakery")
}
