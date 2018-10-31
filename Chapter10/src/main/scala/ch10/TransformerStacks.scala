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

  val buyBaitImpl: String => Future[Bait] = ???
  val castLineImpl: Bait => Either[String, Line] = ???
  val hookFishImpl: Line => Future[Fish] = ???

  override val castLine: Bait => Stack[Line] =
    (bait: Bait) => new EitherT(Future.successful(castLineImpl(bait).map(Option.apply)))

  override val buyBait: String => Stack[Bait] =
    (name: String) => new EitherT(buyBaitImpl(name).map(l => Right(Option(l)): Either[String, Option[Bait]]))

  override val hookFish: Line => Stack[Fish] =
    (line: Line) => new EitherT(hookFishImpl(line).map(l => Right(Option(l)): Either[String, Option[Fish]]))

  val input = optionTunit[Inner, String]("Crankbait")
  val outerResult: Inner[Option[Fish]] = goFishing(input).value
  val innerResult: Future[Either[String, Option[Fish]]] = outerResult.value

}
