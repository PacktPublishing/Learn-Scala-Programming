package ch09

final case class Reader[R, A](run: R => A) {
  def flatMap[B](f: A => Reader[R, B]): Reader[R, B] = Reader { r: R =>
    f(run(r)).run(r)
  }
}

object ReaderExample extends App {
  final case class Limits(speed: Float, angle: Double)
  type ReaderLimits[A] = Reader[Limits, A]

  def go(speed: Float, time: Float)(boat: Boat): ReaderLimits[Boat] = Reader(limits => {
    val lowSpeed = Math.min(speed, limits.speed)
    boat.go(lowSpeed, time)
  })

  def turn(angle: Double)(boat: Boat): ReaderLimits[Boat] = Reader(limits => {
    val smallAngle = Math.min(angle, limits.angle)
    boat.turn(smallAngle)
  })

  import Monad.readerMonad
  import Boat.{move, boat}

  println(move(go, turn)(boat).run(Limits(10f, 0.1)))
}
