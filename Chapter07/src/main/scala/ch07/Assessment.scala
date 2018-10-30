package ch07

import scala.language.higherKinds
import scala.language.reflectiveCalls

import scala.util._

object Assessment {
  implicit val booleanOr: Monoid[Boolean] = new Monoid[Boolean] {
    override def identity: Boolean = false
    override def op(l: Boolean, r: Boolean): Boolean = l || r
  }

  implicit val booleanAnd: Monoid[Boolean] = new Monoid[Boolean] {
    override def identity: Boolean = true
    override def op(l: Boolean, r: Boolean): Boolean = l && r
  }

  implicit def option[A : Monoid]: Monoid[Option[A]] = new Monoid[Option[A]] {
    override def identity: Option[A] = None
    override def op(l: Option[A], r: Option[A]): Option[A] = (l, r) match {
      case (Some(la), Some(lb)) => Option(implicitly[Monoid[A]].op(la, lb))
      case _ => l orElse r
    }
  }

  def either[L, R : Monoid]: Monoid[Either[L, R]] = new Monoid[Either[L, R]] {
    private val ma = implicitly[Monoid[R]]
    override def identity: Either[L, R] = Right(ma.identity)
    override def op(l: Either[L, R], r: Either[L, R]): Either[L, R] = (l, r) match {
      case (l @ Left(_), _) => l
      case (_, l @ Left(_)) => l
      case (Right(la), Right(lb)) => Right(ma.op(la, lb))
    }
  }

}
