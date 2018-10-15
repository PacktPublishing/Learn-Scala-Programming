package ch09

import scala.language.{higherKinds, implicitConversions}
import scala.util.{Failure, Random, Success, Try}
import Boat.{boat, _}
import ch09.WriterExample.WriterTracking

object OptionExample extends App {

  import Monad.optionMonad

  def go(speed: Float, time: Float)(boat: Boat): Option[Boat] =
    if (Random.nextInt(2) == 0) None
    else Option(boat.go(speed, time))

  println(move(go, turn[Option])(Option(boat)))

}

object TryExample extends App {

  import Monad.tryMonad

  def go(speed: Float, time: Float)(boat: Boat): Try[Boat] =
    if (Random.nextInt(100) == 0) Failure(new Exception("Motor malfunction"))
    else Success(boat.go(speed, time))

  println(move(go, turn[Try])(Success(boat)))

}

object EitherExample extends App {

  import Monad.eitherMonad
  type ErrorOr[B] = Either[String, B]

  def go(speed: Float, time: Float)(boat: Boat): ErrorOr[Boat] =
    if (Random.nextInt(100) == 0) Left("Motor malfunction")
    else Right(boat.go(speed, time))

  println(move(go, turn[ErrorOr])(Right(boat)))

}

object WriterOptionExample extends App {
  type WriterOption[B] = Writer[Vector[(Double, Double)], Option[B]]
  import WriterExample.vectorMonoid

  def go(speed: Float, time: Float)(boat: Boat): WriterOption[Boat] = {
    val b: Option[Boat] = OptionExample.go(speed, time)(boat)
    val c: WriterTracking[Boat] = WriterExample.go(speed, time)(boat)
    Writer((b, c.run._2))
  }

  private def writerOption[A](a: A) =
    Writer[Vector[(Double, Double)], Option[A]](Option(a))

  implicit val readerWriterMonad = new Monad[WriterOption] {
    override def flatMap[A, B](wr: WriterOption[A])(f: A => WriterOption[B]): WriterOption[B] =
      wr.compose {
        case Some(a) => f(a)
        case None => Writer(Option.empty[B])
      }

    override def unit[A](a: => A): WriterOption[A] = writerOption(a)
  }

  println(move(go, turn)(writerOption(boat)).run)
}
