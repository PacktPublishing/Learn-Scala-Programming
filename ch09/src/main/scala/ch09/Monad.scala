package ch09

import ch07.Monoid
import ch08.Applicative

import scala.annotation.tailrec
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

  implicit def readerMonad[R] = new Monad[({type T[A] = Reader[R, A]})#T] {
    override def unit[A](a: => A): Reader[R, A] = Reader(_ => a)
    override def flatMap[A, B](a: Reader[R, A])(f: A => Reader[R, B]): Reader[R, B] = a.flatMap(f)
  }

  implicit def writerMonad[W : Monoid] = new Monad[({type T[A] = Writer[W, A]})#T] {
    override def unit[A](a: => A): Writer[W, A] = Writer(() => (a, implicitly[Monoid[W]].identity))
    override def flatMap[A, B](a: Writer[W, A])(f: A => Writer[W, B]): Writer[W, B] = a.flatMap(f)
  }

  object ops {
    implicit def f2monad[A, F[_] : Monad](f: F[A]): Monad[F] = Monad[F]
  }

}

case class State[S, A](run: S => (A, S)) {
  def compose[B](f: A => State[S, B]): State[S, B] = {
    val composedRuns = (s: S) => {
      val (a, nextS) = run(s)
      f(a).run(nextS)
    }
    State(composedRuns)
  }
}

object State {
  def apply[S, A](a: => A): State[S, A] = State(s => (a, s))
  def get[S]: State[S, S] = State(s => (s, s))
  def set[S](s: => S): State[S, Unit] = State(_ => ((), s))
}

// The action of Reader's `flatMap` is to pass the `r` argument along to both the
// outer Reader and also to the result of `f`, the inner Reader. Similar to how
// `State` passes along a state, except that in `Reader` the "state" is read-only.

case class Reader[R, A](run: R => A) {
  def flatMap[B](f: A => Reader[R, B]): Reader[R, B] = Reader { r: R =>
    f(run(r)).run(r)
  }
}

case class Writer[W : Monoid, A](run: () => (A, W)) {
  def flatMap[B](f: A => Writer[W, B]): Writer[W, B] = Writer(run = {
    val (a, w) = run()
    val (b, ww) = f(a).run()
    val www = implicitly[Monoid[W]].op(w, ww)
    () => (b, www)
  })
}
