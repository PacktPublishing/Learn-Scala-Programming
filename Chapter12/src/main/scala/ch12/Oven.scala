package ch12

import akka.actor.typed._
import akka.actor.typed.scaladsl._
import ch12.Baker.{CookiesReady, TooManyCookies}
import ch12.Bakery.{RawCookies, ReadyCookies}

object Oven {
  val size = 12

  sealed trait Command
  final case class Put(rawCookies: Int, sender: ActorRef[Baker.Command])
      extends Command
  final case class Extract(sender: ActorRef[Baker.Command]) extends Command

  def empty: Behaviors.Receive[Command] = Behaviors.receiveMessage[Command] {
    case Put(rawCookies, sender) =>
      val (inside, tooMuch) = insert(rawCookies)
      tooMuch.foreach(sender ! TooManyCookies(_))
      full(inside)
    case Extract(sender) =>
      sender ! CookiesReady(ReadyCookies(0))
      Behaviors.same
  }

  def full(count: Int): Behaviors.Receive[Command] =
    Behaviors.receiveMessage[Command] {
      case Extract(sender) =>
        sender ! CookiesReady(ReadyCookies(count))
        empty
      case Put(rawCookies, sender) =>
        sender ! TooManyCookies(RawCookies(rawCookies))
        Behaviors.same
    }

  def insert(count: Int): (Int, Option[RawCookies]) = {
    val tooMany = math.max(0, count - size)
    val cookiesInside = math.min(size, count)
    (cookiesInside, Some(tooMany).filter(_ > 0).map(RawCookies))
  }

}
