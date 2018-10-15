package ch07

import scala.language.higherKinds

trait Foldable[F[_]] {
  def foldLeft[A,B](as: F[A])(z: B)(f: (B, A) => B): B
  def foldRight[A,B](as: F[A])(z: B)(f: (A, B) => B): B
  def foldMap[A,B : Monoid](as: F[A])(f: A => B): B = {
    val m = implicitly[Monoid[B]]
    foldLeft(as)(m.identity)((b, a) => m.op(f(a), b))
  }
}
