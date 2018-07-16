package ch12

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.Behaviors
import ch12.Bakery.{Groceries, Pastry}
import ch12.Chef.Collect

import scala.concurrent.duration.FiniteDuration

object Mixer {
  final case class Mix(groceries: Groceries, sender: ActorRef[Collect])
  def mix(mixTime: FiniteDuration) = Behaviors.receive[Mix] {
    case (ctx, Mix(Groceries(eggs, flour, sugar, chocolate), sender)) =>
      Thread.sleep(mixTime.toMillis)
      sender ! Collect(Pastry(eggs * 50 + flour + sugar + chocolate), ctx.self)
      Behaviors.stopped
  }
}
/*
  class MotorOverheatException extends Exception
  class SlowRotationSpeedException extends Exception
  class StrongVibrationException extends Exception
 */
