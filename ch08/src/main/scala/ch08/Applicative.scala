package ch08

import scala.language.{higherKinds, reflectiveCalls}
import scala.util.{Failure, Success, Try}

trait Applicative[F[_]] extends Functor[F] {
  def apply[A,B](a: F[A])(f: F[A => B]): F[B]
  def unit[A](a: => A): F[A]

  override def map[A,B](fa: F[A])(f: A => B): F[B] =
    apply(fa)(unit(f))

  def map2[A,B,C](fa: F[A], fb: F[B])(f: (A, B) => C): F[C] =
    apply(fb)(map(fa)(f.curried))

  def map3[A,B,C,D](fa: F[A],
                    fb: F[B],
                    fc: F[C])(f: (A, B, C) => D): F[D] =
    apply(fc)(apply(fb)(apply(fa)(unit(f.curried))))

  def map4[A,B,C,D,E](fa: F[A],
                      fb: F[B],
                      fc: F[C],
                      fd: F[D])(f: (A, B, C, D) => E): F[E] = {
    val ff: (A, B, C) => D => E  = (a,b,c) => d => f(a,b,c,d)
    apply(fd)(map3(fa, fb, fc)(ff))
  }

  def product[G[_]](G: Applicative[G]): Applicative[({type f[x] = (F[x], G[x])})#f] = {
    val F = this
    new Applicative[({type f[x] = (F[x], G[x])})#f] {
      def unit[A](a: => A) = (F.unit(a), G.unit(a))
      override def apply[A,B](p: (F[A], G[A]))(fs: (F[A => B], G[A => B])) =
        (F.apply(p._1)(fs._1), G.apply(p._2)(fs._2))
    }
  }

  def compose[G[_]](G: Applicative[G]): Applicative[({type f[x] = F[G[x]]})#f] = {
    val F = this

    def fab[A, B]: G[A => B] => G[A] => G[B] = (gf: G[A => B]) => (ga: G[A]) => G.apply(ga)(gf)

    def fg[B, A](f: F[G[A => B]]): F[G[A] => G[B]] = F.map(f)(fab)

    new Applicative[({type f[x] = F[G[x]]})#f] {
      def unit[A](a: => A) = F.unit(G.unit(a))
      override def apply[A, B](a: F[G[A]])(f: F[G[A => B]]): F[G[B]] =
        F.apply(a)(fg(f))
    }
  }
}



object Applicative {
  implicit val bucketApplicative: Applicative[List] = new Applicative[List] {

    override def apply[A, B](a: List[A])(f: List[A => B]): List[B] = (a, f) match {
      case (Nil, _) => Nil
      case (_, Nil) => Nil
      case (aa :: as, ff :: fs) =>
        val fab: (A => B) => B = f => f(aa)
        ff(aa) :: as.map(ff) ::: fs.map(fab) ::: apply(as)(fs)
      case other => Nil
    }

    override def unit[A](a: => A): List[A] = List(a)
  }

  implicit val optionApplicative: Applicative[Option] = new Applicative[Option] {
    override def apply[A, B](a: Option[A])(f: Option[A => B]): Option[B] = (a,f) match {
      case (Some(a), Some(f)) => Some(f(a))
      case _ => None
    }
    override def unit[A](a: => A): Option[A] = Some(a)
  }

  implicit def eitherApplicative[L] = new Applicative[({ type T[A] = Either[L, A] })#T] {
    override def apply[A, B](a: Either[L, A])(f: Either[L, A => B]): Either[L, B] = (a, f) match {
      case (Right(a), Right(f)) => Right(f(a))
      case (Left(l), _) => Left(l)
      case (_, Left(l)) => Left(l)
    }
    override def unit[A](a: => A): Either[L, A] = Right(a)
  }

  implicit val tryApplicative: Applicative[Try] = new Applicative[Try] {
    override def apply[A, B](a: Try[A])(f: Try[A => B]): Try[B] = (a, f) match {
      case (Success(a), Success(f)) => Try(f(a))
      case (Failure(ex), _) => Failure(ex)
      case (_, Failure(ex)) => Failure(ex)
    }
    override def unit[A](a: => A): Try[A] = Success(a)
  }

}
