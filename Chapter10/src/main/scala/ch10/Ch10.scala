package ch10

import ch09.Monad
import ch10.Ch10.Fish

import scala.concurrent.Future
import scala.language.higherKinds

object Ch10 {

  type Bate
  type Line
  type Fish

  object ch06 {
    val buyBate: String => Option[Bate] = ???
    val makeBate: String => Option[Bate] = ???
    val castLine: Bate => Option[Line] = ???
    val hookFish: Line => Option[Fish] = ???

    def goFishing(bestBateForFish: Option[String]): Option[Fish] =
      for {
        bateName <- bestBateForFish
        bate <- buyBate(bateName).orElse(makeBate(bateName))
        line <- castLine(bate)
        fish <- hookFish(line)
      } yield fish
  }

  def goFishing[M[_] : Monad, N[_] : Monad](bestBateForFish: M[String]): M[N[M[N[Fish]]]] = {

    val buyBate: String => N[Bate] = ???
    val castLine: Bate => M[Line] = ???
    val hookFish: Line => N[Fish] = ???

    import Monad.lowPriorityImplicits._

    for {
      bateName <- bestBateForFish
    } yield for {
      bate <- buyBate(bateName)
    } yield for {
      line <- castLine(bate)
    } yield for {
      fish <- hookFish(line)
    } yield fish
  }

  object Ch06Combined {

    import scala.concurrent.ExecutionContext.Implicits.global

    val buyBate: String => Future[Bate] = ???
    val castLine: Bate => Option[Line] = ???
    val hookFish: Line => Future[Fish] = ???

    def goFishing(bestBateForFish: Option[String]): Future[Fish] =
      bestBateForFish match {
        case None => Future.failed(new NoSuchElementException)
        case Some(name) => buyBate(name).flatMap { bate: Bate =>
          castLine(bate) match {
            case None => Future.failed(new IllegalStateException)
            case Some(line) => hookFish(line)
          }
        }
      }
  }

  object Ch06Composed {

    import scala.concurrent.ExecutionContext.Implicits.global

    val buyBate: String => Future[Bate] = ???
    val castLine: Bate => Option[Line] = ???
    val hookFish: Line => Future[Fish] = ???

    val buyBateFO: String => Future[Option[Bate]] = (name: String) => buyBate(name).map(Option.apply)
    val castLineFO: Bate => Future[Option[Line]] = castLine.andThen(Future.successful)
    val hookFishFO: Line => Future[Option[Fish]] = (line: Line) => hookFish(line).map(Option.apply)


    def goFishingA(bestBateForFish: Future[Option[String]]): Future[Option[Fish]] =
      bestBateForFish.flatMap {
        case None => Future.successful(Option.empty[Fish])
        case Some(name) => buyBateFO(name).flatMap {
          case None => Future.successful(Option.empty[Fish])
          case Some(bate) => castLineFO(bate).flatMap {
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

    def goFishingB(bestBateForFish: Future[Option[String]]): Future[Option[Fish]] =
      continue(bestBateForFish) { name =>
        continue(buyBateFO(name)) { bate =>
          continue(castLineFO(bate)) { line =>
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

    val buyBate: String => Future[Bate] = ???
    val castLine: Bate => Option[Line] = ???
    val hookFish: Line => Future[Fish] = ???

    val buyBateFO: String => FOption[Future, Bate] = (name: String) => buyBate(name).map(Option.apply)
    val castLineFO: Bate => FOption[Future, Line] = castLine.andThen(line => Future.successful(line))
    val hookFishFO: Line => FOption[Future, Fish] = (line: Line) => hookFish(line).map(Option.apply)


    def goFishingM(bestBateForFish: FOption[Future, String]): FOption[Future, Fish] = for {
      name <- bestBateForFish
      bate <- buyBateFO(name)
      line <- castLineFO(bate)
      fish <- hookFishFO(line)
    } yield fish

  }

}

object Ch09 {

  import scala.concurrent.ExecutionContext.Implicits.global

  val fish: Option[Future[Option[Future[Fish]]]] = Ch10.goFishing[Option, Future](Option("Crankbait"))

}
