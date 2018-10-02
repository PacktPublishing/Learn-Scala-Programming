package ch08

import scala.language.{higherKinds, reflectiveCalls}

trait Applicative[F[_]] extends Functor[F] {
  def apply[A,B](a: F[A])(f: F[A => B]): F[B]
  def unit[A](a: => A): F[A]
}



