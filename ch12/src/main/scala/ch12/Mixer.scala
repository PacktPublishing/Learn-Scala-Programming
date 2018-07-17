package ch12

import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy}
import akka.actor.typed.scaladsl.Behaviors
import ch12.Bakery.{Groceries, Dough}
import ch12.Chef.Collect

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

object Mixer {
  class MotorOverheatException extends Exception
  class SlowRotationSpeedException extends Exception
  class StrongVibrationException extends Exception

  final case class Mix(groceries: Groceries, sender: ActorRef[Collect])

  def mix(mixTime: FiniteDuration): Behavior[Mix] = Behaviors.receive[Mix] {
    case (ctx, Mix(Groceries(eggs, flour, sugar, chocolate), sender)) =>
      if (Random.nextBoolean()) throw new MotorOverheatException
      Thread.sleep(mixTime.toMillis)
      sender ! Collect(Dough(eggs * 50 + flour + sugar + chocolate), ctx.self)
      Behaviors.stopped
  }

  def controlledMix(mixTime: FiniteDuration): Behavior[Mix] =
    Behaviors
      .supervise(
        Behaviors
          .supervise(Behaviors
            .supervise(mix(mixTime))
            .onFailure[MotorOverheatException](SupervisorStrategy.stop))
          .onFailure[SlowRotationSpeedException](SupervisorStrategy.restart))
      .onFailure[StrongVibrationException](SupervisorStrategy.resume)
}
