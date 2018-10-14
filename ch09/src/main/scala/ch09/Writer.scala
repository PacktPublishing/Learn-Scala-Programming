package ch09

import ch07.Monoid

case class Writer[W: Monoid, A](run: (A, W)) {
  def flatMap[B](f: A => Writer[W, B]): Writer[W, B] = Writer {
    val (a, w) = run
    val (b, ww) = f(a).run
    val www = implicitly[Monoid[W]].op(w, ww)
    (b, www)
  }
}

object WriterExample extends App {

  implicit def listMonoid[A]: Monoid[List[A]] = new Monoid[List[A]] {
    override def identity: List[A] = List.empty[A]
    override def op(l: List[A], r: List[A]): List[A] = l ++ r
  }
  type WriterTracking[A] = Writer[List[(Double, Double)], A]

  import Monad.writerMonad

  def go(speed: Float, time: Float)(boat: Boat): WriterTracking[Boat] =
    new WriterTracking((boat.go(speed, time), List(boat.position)))

  import Boat.{move, boat, turn}

  println(move(go, turn[WriterTracking])(boat).run)
}
