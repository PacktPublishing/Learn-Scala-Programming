package ch12
import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{Behaviors, StashBuffer}
import ch12.Bakery.{RawCookies, ReadyCookies}
import ch12.Manager.ReceiveReadyCookies
import ch12.Oven.{Extract, Put}

import scala.concurrent.duration._

object Baker {
  val DefaultBakingTime: FiniteDuration = 2.seconds
  private val TimerKey = 'TimerKey
  sealed trait Command
  final case class BakeCookies(raw: RawCookies,
                               sender: ActorRef[Manager.Command])
      extends Command
  final case class TooManyCookies(raw: RawCookies) extends Command
  final case class CookiesReady(cookies: ReadyCookies) extends Command
  final case object CheckOven extends Command

  def turnOvenOn: Behavior[Command] = Behaviors.setup { context =>
    val oven = context.spawn(Oven.empty, "Oven")
    idle(oven)
  }

  def idle(oven: ActorRef[Oven.Command]): Behavior[Command] =
    Behaviors.receivePartial {
      case (context, BakeCookies(rawCookies, manager)) =>
        oven ! Put(rawCookies.count, context.self)
        Behaviors.withTimers { timers =>
          timers.startSingleTimer(TimerKey, CheckOven, DefaultBakingTime)
          baking(oven, manager)
        }
    }

  def baking(oven: ActorRef[Oven.Command],
             manager: ActorRef[Manager.Command]): Behavior[Command] =
    Behaviors.setup[Command] { context =>
      val buffer = StashBuffer[Command](capacity = 100)

      Behaviors.receiveMessage {
        case CheckOven =>
          oven ! Extract(context.self)
          Behaviors.same
        case CookiesReady(cookies) =>
          manager ! ReceiveReadyCookies(cookies)
          buffer.unstashAll(context, idle(oven))
        case c: TooManyCookies=>
          buffer.stash(BakeCookies(c.raw, manager))
          Behaviors.same
        case c : BakeCookies =>
          buffer.stash(c)
          Behaviors.same
      }
    }
}
