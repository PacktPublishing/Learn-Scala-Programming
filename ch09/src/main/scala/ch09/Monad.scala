package ch09

import ch07.Monoid
import ch08.Applicative

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.language.{higherKinds, implicitConversions}
import scala.util.{Failure, Success, Try}


trait Monad[F[_]] extends Applicative[F] {
  def flatMap[A, B](a: F[A])(f: A => F[B]): F[B]

  def flatten[A](a: F[F[A]]): F[A] = flatMap(a)(identity)

  override def unit[A](a: => A): F[A]

  override def map[A, B](a: F[A])(f: A => B): F[B] = flatMap(a)(a => unit(f(a)))

  override def apply[A, B](a: F[A])(f: F[A => B]): F[B] =
    flatMap(f) { fab: (A => B) => map(a) { a: A => fab(a) }}
}

object Monad {
  def apply[F[_] : Monad]: Monad[F] = implicitly[Monad[F]]

  type Id[A] = A

  implicit val idMonad = new Monad[Id] {
    override def unit[A](a: => A): Id[A] = a
    override def flatMap[A, B](a: Id[A])(f: A => Id[B]): Id[B] = f(a)
  }

  implicit val optionMonad = new Monad[Option] {
    override def unit[A](a: => A): Option[A] = Some(a)

    override def flatMap[A, B](a: Option[A])(f: A => Option[B]): Option[B] = a match {
      case Some(value) => f(value)
      case _ => None
    }
  }

  implicit val tryMonad = new Monad[Try] {
    override def unit[A](a: => A): Try[A] = Success(a)

    override def flatMap[A, B](a: Try[A])(f: A => Try[B]): Try[B] = a match {
      case Success(value) => f(value)
      case Failure(ex) => Failure(ex)
    }
  }

  implicit def eitherMonad[L] = new Monad[Either[L, ?]] {
    override def unit[A](a: => A): Either[L, A] = Right(a)

    override def flatMap[A, B](a: Either[L, A])(f: A => Either[L, B]): Either[L, B] = a match {
      case Right(r) => f(r)
      case Left(l) => Left(l)
    }
  }

  implicit val listMonad = new Monad[List] {
    def unit[A](a: => A) = List(a)

    def flatMapNonTailRec[A,B](as: List[A])(f: A => List[B]): List[B] = as match {
      case Nil => Nil
      case a :: as => f(a) ::: flatMap(as)(f)
    }

    def flatMapOkButSlow[A,B](as: List[A])(f: A => List[B]): List[B] = {
      @tailrec
      def fMap(as: List[A], acc: List[B])(f: A => List[B]): List[B] = as match {
        case Nil => acc
        case a :: aas => fMap(aas, acc ::: f(a))(f)
      }
      fMap(as, Nil)(f)
    }

    override def flatMap[A,B](as: List[A])(f: A => List[B]): List[B] = as.flatMap(f)
  }

  implicit def stateMonad[S] = new Monad[State[S, ?]] {
    override def unit[A](a: => A): State[S, A] = State(a)
    override def flatMap[A, B](a: State[S, A])(f: A => State[S, B]): State[S, B] = a.compose(f)
  }

  implicit def readerMonad[R] = new Monad[Reader[R, ?]] {
    override def unit[A](a: => A): Reader[R, A] = Reader(a)
    override def flatMap[A, B](a: Reader[R, A])(f: A => Reader[R, B]): Reader[R, B] = a.compose(f)
  }

  implicit def writerMonad[W : Monoid] = new Monad[Writer[W, ?]] {
    override def unit[A](a: => A): Writer[W, A] = Writer(a)
    override def flatMap[A, B](a: Writer[W, A])(f: A => Writer[W, B]): Writer[W, B] = a.compose(f)
  }

  // strictly speaking, Future is not a monad because it does not satisfy monadic laws
  implicit def futureMonad(implicit ec: ExecutionContext) = new Monad[Future] {
    override def unit[A](a: => A): Future[A] = Future(a)
    override def flatMap[A, B](a: Future[A])(f: A => Future[B]): Future[B] =
      a.flatMap(f)
  }

  object lowPriorityImplicits {
    implicit class MonadF[A, F[_] : Monad](val value: F[A]) {
      private val M = implicitly[Monad[F]]
      def unit(a: A) = M.unit(a)
      def flatMap[B](fab: A => F[B]): F[B] = M.flatMap(value)(fab)
      def map[B](fab: A => B): F[B] = M.map(value)(fab)
    }
  }

}

