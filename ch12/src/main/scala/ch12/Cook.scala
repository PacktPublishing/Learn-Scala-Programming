package ch12

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import ch12.Bakery.{Pastry, RawCookies}
import ch12.Manager.ReceiveRawCookies

object Cook {
  final case class FormCookies(pastry: Pastry, sender: ActorRef[Manager.Command])

  val form: Behaviors.Receive[FormCookies] = Behaviors.receiveMessage {
    case FormCookies(pastry, sender) =>
      val numberOfCookies = makeCookies(pastry.weight)
      sender ! ReceiveRawCookies(RawCookies(numberOfCookies))
      form
  }

  private val cookieWeight = 60
  private def makeCookies(weight: Int): Int = weight / cookieWeight
}
