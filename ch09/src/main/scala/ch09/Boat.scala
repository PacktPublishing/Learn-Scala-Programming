package ch09

final case class Boat(direction: Double, position: (Double, Double)) {
  def go(speed: Float, time: Float): Boat = {
    val distance = speed * time
    val (x, y) = position
    val nx = x + distance * Math.cos(direction)
    val ny = y + distance * Math.sin(direction)
    copy(direction, (nx, ny))
  }
  def turn(angle: Double): Boat =
    copy(direction = (this.direction + angle) % (2 * Math.PI))
}

import scala.language.{higherKinds, implicitConversions}

object Boat {
  import Monad.lowPriorityImplicits._

  def go[M[_]: Monad]: (Float, Float) => Boat => M[Boat] =
    (speed, time) => boat => Monad[M].unit(boat.go(speed, time))

  def turn[M[_]: Monad]: Double => Boat => M[Boat] =
    angle => boat => Monad[M].unit(boat.turn(angle))

  def move[M[_]: Monad](go: (Float, Float) => Boat => M[Boat], turn: Double => Boat => M[Boat])(boat: Boat): M[Boat] = for {
    a <- Monad[M].unit(boat)
    b <- go(10,5)(a)
    c <- turn(0.5)(b)
    d <- go(20, 20)(c)
    e <- turn(-0.1)(d)
    f <- go(1,1)(e)
  } yield f

  val boat = Boat(0, (0d, 0d))
}
