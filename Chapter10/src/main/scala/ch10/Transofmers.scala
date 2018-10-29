package ch10

import ch09.Monad
import ch09.Monad.lowPriorityImplicits._
import ch10.Ch10.{Bate, Fish, Line}
import ch10.Transformers.{EitherT, OptionT}

import scala.concurrent.Future
import scala.language.higherKinds
import scala.util.Try

object Transformers {

  // ------- OptionT -------

  private def noResultOptionT[F[_]: Monad, T]: F[Option[T]] = Monad[F].unit(Option.empty[T])

  implicit class OptionT[F[_]: Monad, A](val value: F[Option[A]]) {
    def compose[B](f: A => OptionT[F, B]): OptionT[F, B] = {
      val result = value.flatMap {
        case None => noResultOptionT[F, B]
        case Some(a) => f(a).value
      }
      new OptionT(result)
    }
    def isEmpty: F[Boolean] = Monad[F].map(value)(_.isEmpty)
  }

  def optionTunit[F[_]: Monad, A](a: => A) = new OptionT(Monad[F].unit(Option(a)))

  implicit def OptionTMonad[F[_] : Monad]: Monad[OptionT[F, ?]] = new Monad[OptionT[F, ?]] {
    override def unit[A](a: => A): OptionT[F, A] = Monad[F].unit(Monad[Option].unit(a))
    override def flatMap[A, B](a: OptionT[F, A])(f: A => OptionT[F, B]): OptionT[F, B] = a.compose(f)
  }

  // ------- EitherT -------

  implicit class EitherT[F[_]: Monad, L, A](val value: F[Either[L, A]]) {
    def compose[B](f: A => EitherT[F, L, B]): EitherT[F, L, B] = {
      val result: F[Either[L, B]] = value.flatMap {
        case Left(l) => Monad[F].unit(Left[L, B](l))
        case Right(a) => f(a).value
      }
      new EitherT(result)
    }
    def isRight: F[Boolean] = Monad[F].map(value)(_.isRight)
  }

  def eitherTunit[F[_]: Monad, L, A](a: => A) = new EitherT[F, L, A](Monad[F].unit(Right(a)))

  implicit def EitherTMonad[F[_] : Monad, L]: Monad[EitherT[F, L, ?]] = new Monad[EitherT[F, L, ?]] {
    override def unit[A](a: => A): EitherT[F, L, A] = Monad[F].unit(ch09.Monad.eitherMonad[L].unit(a))
    override def flatMap[A, B](a: EitherT[F, L, A])(f: A => EitherT[F, L, B]): EitherT[F, L, B] = a.compose(f)
  }
}


abstract class FishingApi[F[_]: Monad] {

  val buyBate: String => F[Bate]
  val castLine: Bate => F[Line]
  val hookFish: Line => F[Fish]

  def goFishing(bestBateForFish: F[String]): F[Fish] = for {
    name <- bestBateForFish
    bate <- buyBate(name)
    line <- castLine(bate)
    fish <- hookFish(line)
  } yield fish
}

import Transformers.OptionTMonad
import ch09.Monad.futureMonad
import scala.concurrent.ExecutionContext.Implicits.global

object Ch10FutureFishing extends FishingApi[OptionT[Future, ?]] {

  val buyBateImpl: String => Future[Bate] = ???
  val castLineImpl: Bate => Option[Line] = ???
  val hookFishImpl: Line => Future[Fish] = ???

  override val buyBate: String => OptionT[Future, Bate] = (name: String) => buyBateImpl(name).map(Option.apply)
  override val castLine: Bate => OptionT[Future, Line] = castLineImpl.andThen(Future.successful(_))
  override val hookFish: Line => OptionT[Future, Fish] = (line: Line) => hookFishImpl(line).map(Option.apply)

  goFishing(Transformers.optionTunit[Future, String]("Crankbait"))

}

object Ch10OptionTTryFishing extends FishingApi[OptionT[Try, ?]] {

  val buyBateImpl: String => Try[Bate] = ???
  val castLineImpl: Bate => Option[Line] = ???
  val hookFishImpl: Line => Try[Fish] = ???

  override val buyBate: String => OptionT[Try, Bate] = (name: String) => buyBateImpl(name).map(Option.apply)
  override val castLine: Bate => OptionT[Try, Line] = castLineImpl.andThen(Try.apply(_))
  override val hookFish: Line => OptionT[Try, Fish] = (line: Line) => hookFishImpl(line).map(Option.apply)

  goFishing(Transformers.optionTunit[Try, String]("Crankbait"))

}

object Ch10EitherTFutureFishing extends FishingApi[EitherT[Future, String, ?]] {

  val buyBateImpl: String => Future[Bate] = ???
  val castLineImpl: Bate => Either[String, Line] = ???
  val hookFishImpl: Line => Future[Fish] = ???

  override val buyBate: String => EitherT[Future, String, Bate] =
    (name: String) => buyBateImpl(name).map(l => Right(l): Either[String, Bate])
  override val castLine: Bate => EitherT[Future, String, Line] =
    castLineImpl.andThen(Future.successful(_))
  override val hookFish: Line => EitherT[Future, String, Fish] =
    (line: Line) => hookFishImpl(line).map(l => Right(l): Either[String, Fish])

  goFishing(Transformers.eitherTunit[Future, String, String]("Crankbait"))

}
