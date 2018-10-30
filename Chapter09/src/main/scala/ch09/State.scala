package ch09

import scala.language.higherKinds

final case class State[S, A](run: S => (A, S)) {
  def compose[B](f: A => State[S, B]): State[S, B] = {
    val composedRuns = (s: S) => {
      val (a, nextState) = run(s)
      f(a).run(nextState)
    }
    State(composedRuns)
  }
}

object State {
  def apply[S, A](a: => A): State[S, A] = State(s => (a, s))
  def get[S]: State[S, S] = State(s => (s, s))
  def set[S](s: => S): State[S, Unit] = State(_ => ((), s))
}

object StateExample extends App {
  lazy val consumption = 1f
  type FuelState = State[Float, Boat]

  def consume(speed: Float, time: Float) = consumption * time * speed

  def go(speed: Float, time: Float)(boat: Boat): FuelState = new State(fuel => {
    val newFuel = fuel - consume(speed, time)
    (boat.go(speed, time), newFuel)
  })

  def turn(angle: Double)(boat: Boat): FuelState = State(boat.turn(angle))

  import Boat.boat

  println(boat.go(10, 5).turn(0.5).go(20, 20).turn(-0.1).go(1,1))

  import Monad.lowPriorityImplicits._

  def move(boat: Boat) = State[Float, Boat](boat).
    flatMap(go(10, 5)).
    flatMap(turn(0.5)).
    flatMap(go(20,20)).
    flatMap(turn(-0.1)).
    flatMap{b: Boat => go(1,1)(b)}

  def mv(boat: Boat) = for {
    a <- State[Float, Boat](boat)
    f1 <- State.get[Float]
    _ = logFuelState(f1)
    _ <- State.set(Math.min(700, f1))
    b <- go(10,5)(a)
    f2 <- State.get[Float]; _ = logFuelState(f2)
    c <- turn(0.5)(b)
    f3 <- State.get[Float]; _ = logFuelState(f3)
    d <- go(20, 20)(c)
    f3 <- State.get[Float]; _ = logFuelState(f3)
    e <- turn(-0.1)(d)
    f3 <- State.get[Float]; _ = logFuelState(f3)
    f <- go(1,1)(e)
  } yield f

  println(move(boat).run(1000f))
  println(mv(boat).run(1000f))

  def logFuelState(f: Float) = println(s"Current fuel level is $f")
}


object PolymorphicStateExample1 extends App {
  import StateExample.consume
  type FuelStateBoat = State[Float, Boat]

  def go[M[_]: Monad](speed: Float, time: Float)(boat: Boat): FuelStateBoat = new State((fuel: Float) => {
    val newFuel = fuel - consume(speed, time)
    (boat.go(speed, time), newFuel)
  })

  def turn(angle: Double)(boat: Boat): FuelStateBoat = State(boat.turn(angle))

  import Monad.stateMonad
  import Monad.lowPriorityImplicits._

  def move(boat: Boat): State[Float, Boat] = for {
    a <- State[Float, Boat](boat)
    i <- State.get[Float]
    _ <- State.set(Math.min(700, i))
    b <- go(10,5)(a)
    c <- turn(0.5)(b)
    d <- go(20, 20)(c)
    e <- turn(-0.1)(d)
    f <- go(1,1)(e)
  } yield f

  println(move(Boat.boat).run(1000f))
}

object PolymorphicStateExample2 extends App {

  import StateExample.consume

  def go(speed: Float, time: Float)(boat: Boat): State[Float, Boat] = new State((fuel: Float) => {
    val newFuel = fuel - consume(speed, time)
    (boat.go(speed, time), newFuel)
  })

  import Monad.stateMonad
  import Boat.{move, turn, boat}
  type FuelState[B] = State[Float, B]
  println(move(go, turn[FuelState])(State(boat)).run(1000f))
}
