package ch10

import ch09.Monad
import ch09.Monad.lowPriorityImplicits._
import ch10.Ch10.{Bate, Fish, Line}
import ch10.Ch10OptionTEitherTFutureFishing.goFishing
import ch10.TransformerStacks.{Inner, Stack}
import ch10.Transformers.{EitherT, OptionT, eitherTunit}

import scala.concurrent.Future
import scala.language.higherKinds
import scala.util.{Failure, Success, Try}

object Assessments {

  private def noResultTryT[F[_] : Monad, T](ex: Throwable): F[Try[T]] = Monad[F].unit(Failure[T](ex))

  implicit class TryT[F[_] : Monad, A](val value: F[Try[A]]) {
    def compose[B](f: A => TryT[F, B]): TryT[F, B] = {
      val result = value.flatMap {
        case Failure(ex) => noResultTryT[F, B](ex)
        case Success(a) => f(a).value
      }
      new TryT(result)
    }

    def isSuccess: F[Boolean] = Monad[F].map(value)(_.isSuccess)
  }

  def tryTunit[F[_] : Monad, A](a: => A) = new TryT(Monad[F].unit(Try(a)))

  implicit def TryTMonad[F[_] : Monad]: Monad[TryT[F, ?]] = new Monad[TryT[F, ?]] {
    override def unit[A](a: => A): TryT[F, A] = Monad[F].unit(Monad[Try].unit(a))

    override def flatMap[A, B](a: TryT[F, A])(f: A => TryT[F, B]): TryT[F, B] = a.compose(f)
  }


  import ch09.Monad.futureMonad
  import scala.concurrent.ExecutionContext.Implicits.global

  object Ch10FutureTryFishing extends FishingApi[TryT[Future, ?]] {

    val buyBateImpl: String => Future[Bate] = ???
    val castLineImpl: Bate => Try[Line] = ???
    val hookFishImpl: Line => Future[Fish] = ???

    override val buyBate: String => TryT[Future, Bate] = (name: String) => buyBateImpl(name).map(Try(_))
    override val castLine: Bate => TryT[Future, Line] = castLineImpl.andThen(Future.successful(_))
    override val hookFish: Line => TryT[Future, Fish] = (line: Line) => hookFishImpl(line).map(Try(_))

    val result: Future[Try[Fish]] = goFishing(tryTunit[Future, String]("Crankbait")).value

  }

  // Transformer Stack

  type Inner[A] = OptionT[Future, A]
  type Outer[F[_], A] = EitherT[F, String, A]
  type Stack[A] = Outer[Inner, A]

  object Ch10EitherTOptionTFutureFishing extends FishingApi[Stack[?]] {

    val buyBateImpl: String => Future[Bate] = ???
    val castLineImpl: Bate => Either[String, Line] = ???
    val hookFishImpl: Line => Future[Fish] = ???

    override val castLine: Bate => Stack[Line] =
      (bate: Bate) => new OptionT(Future.successful(Option(castLineImpl(bate))))

    override val buyBate: String => Stack[Bate] =
      (name: String) => new OptionT(buyBateImpl(name).map(l => Option(Right(l)): Option[Either[String, Bate]]))

    override val hookFish: Line => Stack[Fish] =
      (line: Line) => new OptionT(hookFishImpl(line).map(l => Option(Right(l)): Option[Either[String, Fish]]))

    val input: EitherT[Inner, String, String] = eitherTunit[Inner, String, String]("Crankbait")
    val outerResult: Inner[Either[String, Fish]] = goFishing(input).value
    val innerResult: Future[Option[Either[String, Fish]]] = outerResult.value

  }

}
