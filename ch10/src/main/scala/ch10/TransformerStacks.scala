package ch10

import ch10.Ch10._

import scala.concurrent.Future
import scala.language.higherKinds
import scala.concurrent.ExecutionContext.Implicits.global
import ch10.Transformers._
import TransformerStacks._

object TransformerStacks {

  type Inner[A] = EitherT[Future, String, A]
  type Outer[F[_], A] = OptionT[F, A]
  type Stack[A] = Outer[Inner, A]

}

object Ch10OptionTEitherTFutureFishing extends FishingApi[Stack[?]] {

  val buyBateImpl: String => Future[Bate] = ???
  val castLineImpl: Bate => Either[String, Line] = ???
  val hookFishImpl: Line => Future[Fish] = ???

  override val castLine: Bate => Stack[Line] =
    (bate: Bate) => new EitherT(Future.successful(castLineImpl(bate).map(Option.apply)))

  override val buyBate: String => Stack[Bate] =
    (name: String) => new EitherT(buyBateImpl(name).map(l => Right(Option(l)): Either[String, Option[Bate]]))

  override val hookFish: Line => Stack[Fish] =
    (line: Line) => new EitherT(hookFishImpl(line).map(l => Right(Option(l)): Either[String, Option[Fish]]))

  val input = optionTunit[Inner, String]("Crankbait")
  val outerResult: Inner[Option[Fish]] = goFishing(input).value
  val innerResult: Future[Either[String, Option[Fish]]] = outerResult.value

}
