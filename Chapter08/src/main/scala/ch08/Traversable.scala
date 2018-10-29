package ch08

import ch08.Model.Bucket

import scala.language.{higherKinds, reflectiveCalls}
import scala.util.{Failure, Success, Try}
import scala.{Traversable => _}

trait Traversable[F[_]] extends Functor[F] {
  def traverse[A,B,G[_]: Applicative](a: F[A])(f: A => G[B]): G[F[B]]
  def sequence[A,G[_]: Applicative](a: F[G[A]]): G[F[A]] = traverse(a)(identity)

  implicit def compose[H[_]](implicit H: Traversable[H]): Traversable[({type f[x] = F[H[x]]})#f] = {
    val F = this
    new Traversable[({type f[x] = F[H[x]]})#f] {
      override def traverse[A, B, G[_] : Applicative](fa: F[H[A]])(f: A => G[B]) =
        F.traverse(fa)((ga: H[A]) => H.traverse(ga)(f))

      override def map[A, B](in: F[H[A]])(f: A => B): F[H[B]] =
        F.map(in)((ga: H[A]) => H.map(ga)(f))
    }
  }
}

object Traversable {

  implicit val bucketTraversable = new Traversable[Bucket] {
    override def map[A, B](in: Bucket[A])(f: A => B): Bucket[B] = Functor.bucketFunctor.map(in)(f)
    override def traverse[A, B, G[_] : Applicative](a: Bucket[A])(f: A => G[B]): G[Bucket[B]] = {
      val G = implicitly[Applicative[G]]
      a.foldRight(G.unit(List[B]()))((aa, fbs) => G.map2(f(aa), fbs)(_ :: _))
    }
  }

  implicit val optionTraversable = new Traversable[Option] {
    override def map[A, B](in: Option[A])(f: A => B): Option[B] = Functor.optionFunctor.map(in)(f)
    override def traverse[A, B, G[_] : Applicative](a: Option[A])(f: A => G[B]): G[Option[B]] = {
      val G = implicitly[Applicative[G]]
      a match {
        case Some(s) => G.map(f(s))(Some.apply)
        case None => G.unit(None)
      }
    }
  }

  implicit val tryTraversable = new Traversable[Try] {
    override def map[A, B](in: Try[A])(f: A => B): Try[B] = Functor.tryFunctor.map(in)(f)
    override def traverse[A, B, G[_] : Applicative](a: Try[A])(f: A => G[B]): G[Try[B]] = {
      val G = implicitly[Applicative[G]]
      a match {
        case Success(s) => G.map(f(s))(Success.apply)
        case Failure(ex) => G.unit(Failure(ex)) // re-wrap the ex to change the type of Failure
      }
    }
  }

  implicit def eitherTraversable[L] = new Traversable[({ type T[A] = Either[L, A] })#T] {
    override def map[A, B](in: Either[L, A])(f: A => B): Either[L, B] = Functor.eitherFunctor[L].map(in)(f)
    override def traverse[A, B, G[_] : Applicative](a: Either[L, A])(f: A => G[B]): G[Either[L, B]] = {
      val G = implicitly[Applicative[G]]
      a match {
        case Right(s) => G.map(f(s))(Right.apply)
        case Left(l) => G.unit(Left(l)) // re-wrap the l to change the type of Failure
      }
    }
  }

}
