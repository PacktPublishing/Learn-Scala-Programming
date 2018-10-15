package ch09

import ch07.Monoid

final case class Writer[W: Monoid, A](run: (A, W)) {
  def compose[B](f: A => Writer[W, B]): Writer[W, B] = Writer {
    val (a, w) = run
    val (b, ww) = f(a).run
    val www = implicitly[Monoid[W]].op(w, ww)
    (b, www)
  }
}

object Writer {
  def apply[W: Monoid, A](a: => A): Writer[W, A] = Writer((a, implicitly[Monoid[W]].identity))
}


object WriterExample extends App {

  implicit def vectorMonoid[A]: Monoid[Vector[A]] = new Monoid[Vector[A]] {
    override def identity: Vector[A] = Vector.empty[A]
    override def op(l: Vector[A], r: Vector[A]): Vector[A] = l ++ r
  }

  type WriterTracking[A] = Writer[Vector[(Double, Double)], A]

  def go(speed: Float, time: Float)(boat: Boat): WriterTracking[Boat] =
    new WriterTracking((boat.go(speed, time), Vector(boat.position)))

  import Monad.writerMonad
  import Boat.{move, boat, turn}

  println(move(go, turn[WriterTracking])(Writer(boat)).run)
}
