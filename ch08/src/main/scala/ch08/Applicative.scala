package ch08

import scala.language.{higherKinds, reflectiveCalls}
import scala.util.{Failure, Success, Try}

trait Applicative[F[_]] extends Functor[F] {
  def apply[A,B](a: F[A])(f: F[A => B]): F[B]
  def unit[A](a: => A): F[A]
}



object Applicative {
  implicit val bucketApplicative: Applicative[List] = new Applicative[List] {
    override def map[A, B](in: List[A])(f: A => B): List[B] = in.map(f)

    override def mapC[A, B](f: A => B): List[A] => List[B] = (_: List[A]).map(f)

    override def apply[A, B](a: List[A])(f: List[A => B]): List[B] = (a, f) match {
      case (Nil, _) => Nil
      case (_, Nil) => Nil
      case (aa :: as, ff :: fs) =>
        val fab: (A => B) => B = f => f(aa)
        ff(aa) :: as.map(ff) ::: fs.map(fab) ::: apply(as)(fs)
      case orher => Nil
    }

    override def unit[A](a: => A): List[A] = List(a)
  }

  implicit val optionApplicative: Applicative[Option] = new Applicative[Option] {
    override def map[A, B](in: Option[A])(f: A => B): Option[B] = in.map(f)
    override def mapC[A, B](f: A => B): Option[A] => Option[B] = (_: Option[A]).map(f)

    override def apply[A, B](a: Option[A])(f: Option[A => B]): Option[B] = (a,f) match {
      case (Some(a), Some(f)) => Some(f(a))
      case _ => None
    }

    override def unit[A](a: => A): Option[A] = Some(a)
  }

  implicit def eitherApplicative[L] = new Applicative[({ type T[A] = Either[L, A] })#T] {
    override def map[A, B](in: Either[L, A])(f: A => B): Either[L, B] = in.map(f)
    override def mapC[A, B](f: A => B): Either[L, A] => Either[L, B] = (_: Either[L, A]).map(f)

    override def apply[A, B](a: Either[L, A])(f: Either[L, A => B]): Either[L, B] = (a, f) match {
      case (Right(a), Right(f)) => Right(f(a))
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
    }


    override def unit[A](a: => A): Either[L, A] = Right(a)
  }

  implicit val tryApplicative: Applicative[Try] = new Applicative[Try] {
    override def map[A, B](in: Try[A])(f: A => B): Try[B] = in.map(f)
    override def mapC[A, B](f: A => B): Try[A] => Try[B] = (_: Try[A]).map(f)

    override def apply[A, B](a: Try[A])(f: Try[A => B]): Try[B] = (a, f) match {
      case (Success(a), Success(f)) => Try(f(a))
      case (Failure(ex), _) => Failure(ex)
      case (_, Failure(ex)) => Failure(ex)
    }

    override def unit[A](a: => A): Try[A] = Success(a)
  }

}
