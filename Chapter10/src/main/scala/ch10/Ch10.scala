package ch10

import ch09.Monad
import ch10.Ch10.Fish

import scala.concurrent.Future
import scala.language.higherKinds

object Ch10 {

  type Bait = String
  type Line = String
  type Fish = String

  object ch06 {
    val buyBait: String => Option[Bait] = Option.apply
    val makeBait: String => Option[Bait] = Option.apply
    val castLine: Bait => Option[Line] = Option.apply
    val hookFish: Line => Option[Fish] = Option.apply

    def goFishing(bestBaitForFish: Option[String]): Option[Fish] =
      for {
        baitName <- bestBaitForFish
        bait <- buyBait(baitName).orElse(makeBait(baitName))
        line <- castLine(bait)
        fish <- hookFish(line)
      } yield fish
  }

  def goFishing[M[_] : Monad, N[_] : Monad](bestBaitForFish: M[String]): M[N[M[N[Fish]]]] = {

    val buyBait: String => N[Bait] = ???
    val castLine: Bait => M[Line] = ???
    val hookFish: Line => N[Fish] = ???

    import Monad.lowPriorityImplicits._

    for {
      baitName <- bestBaitForFish
    } yield for {
      bait <- buyBait(baitName)
    } yield for {
      line <- castLine(bait)
    } yield for {
      fish <- hookFish(line)
    } yield fish
  }

  object Ch06Combined {

    import scala.concurrent.ExecutionContext.Implicits.global

    val buyBait: String => Future[Bait] = Future.successful
    val castLine: Bait => Option[Line] = Option.apply
    val hookFish: Line => Future[Fish] = Future.successful

    def goFishing(bestBaitForFish: Option[String]): Future[Fish] =
      bestBaitForFish match {
        case None => Future.failed(new NoSuchElementException)
        case Some(name) => buyBait(name).flatMap { bait: Bait =>
          castLine(bait) match {
            case None => Future.failed(new IllegalStateException)
            case Some(line) => hookFish(line)
          }
        }
      }
  }

  object Ch06Composed {

    import scala.concurrent.ExecutionContext.Implicits.global

    val buyBait: String => Future[Bait] = ???
    val castLine: Bait => Option[Line] = ???
    val hookFish: Line => Future[Fish] = ???

    val buyBaitFO: String => Future[Option[Bait]] = (name: String) => buyBait(name).map(Option.apply)
    val castLineFO: Bait => Future[Option[Line]] = castLine.andThen(Future.successful)
    val hookFishFO: Line => Future[Option[Fish]] = (line: Line) => hookFish(line).map(Option.apply)


    def goFishingA(bestBaitForFish: Future[Option[String]]): Future[Option[Fish]] =
      bestBaitForFish.flatMap {
        case None => Future.successful(Option.empty[Fish])
        case Some(name) => buyBaitFO(name).flatMap {
          case None => Future.successful(Option.empty[Fish])
          case Some(bait) => castLineFO(bait).flatMap {
            case None => Future.successful(Option.empty[Fish])
            case Some(line) => hookFishFO(line)
          }
        }
      }


    private def noResult[T]: Future[Option[T]] = Future.successful(Option.empty[T])

    def continue[A, B](arg: Future[Option[A]])(f: A => Future[Option[B]]): Future[Option[B]] = arg.flatMap {
      case None => noResult[B]
      case Some(a) => f(a)
    }

    def goFishingB(bestBaitForFish: Future[Option[String]]): Future[Option[Fish]] =
      continue(bestBaitForFish) { name =>
        continue(buyBaitFO(name)) { bait =>
          continue(castLineFO(bait)) { line =>
            hookFishFO(line)
          }
        }
      }

  }
    object Ch06Transformed {
      import scala.concurrent.ExecutionContext.Implicits.global
      import Monad.lowPriorityImplicits._

      private def noResultF[F[_]: Monad, T] = Monad[F].unit(Option.empty[T])

    implicit class FOption[F[_]: Monad, A](val value: F[Option[A]]) {
      def compose[B](f: A => FOption[F, B]): FOption[F, B] = {
        val result = value.flatMap {
          case None => noResultF[F, B]
          case Some(a) => f(a).value
        }
        new FOption(result)
      }
      def isEmpty: F[Boolean] = Monad[F].map(value)(_.isEmpty)
    }

    implicit def fOptionMonad[F[_] : Monad] = new Monad[FOption[F, ?]] {
      override def unit[A](a: => A): FOption[F, A] = Monad[F].unit(Monad[Option].unit(a))
      override def flatMap[A, B](a: FOption[F, A])(f: A => FOption[F, B]): FOption[F, B] = a.compose(f)
    }

    val buyBait: String => Future[Bait] = ???
    val castLine: Bait => Option[Line] = ???
    val hookFish: Line => Future[Fish] = ???

    val buyBaitFO: String => FOption[Future, Bait] = (name: String) => buyBait(name).map(Option.apply)
    val castLineFO: Bait => FOption[Future, Line] = castLine.andThen(line => Future.successful(line))
    val hookFishFO: Line => FOption[Future, Fish] = (line: Line) => hookFish(line).map(Option.apply)


    def goFishingM(bestBaitForFish: FOption[Future, String]): FOption[Future, Fish] = for {
      name <- bestBaitForFish
      bait <- buyBaitFO(name)
      line <- castLineFO(bait)
      fish <- hookFishFO(line)
    } yield fish

  }

}

object Ch09 {

  import scala.concurrent.ExecutionContext.Implicits.global

  val fish: Option[Future[Option[Future[Fish]]]] = Ch10.goFishing[Option, Future](Option("Crankbait"))

}
