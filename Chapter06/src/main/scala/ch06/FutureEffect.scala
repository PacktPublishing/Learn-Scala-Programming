package ch06

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

trait FutureEffect {
  val runningForever = Future {
    while (true) Thread.sleep(1000)
  }
  val stringFuture = Future.successful("Well")
  val failure = Future.failed(new IllegalArgumentException)
  val fromTry = Future.fromTry(Try(10 / 0))

  val runningLong = Future.unit.map { _ =>
    while (math.random() > 0.001) Thread.sleep(100)
  }

  runningLong.foreach(_ => println("First callback"))
  runningLong.foreach(_ => println("Second callback"))

  runningLong.onComplete {
    case Success(value) => println(s"Success with $value")
    case Failure(ex) => println(s"Failure with $ex")
  }

  stringFuture.transform(_.length, ex => new Exception(ex))
  stringFuture.transform {
    case Success(value) => Success(value.length)
    case Failure(ex) => Failure(new Exception(ex))
  }

  stringFuture.filter(_.length > 10)

  stringFuture.collect {
    case s if s.length > 10 => s.toUpperCase
  }

  if (runningLong.isCompleted) runningLong.value

  runningForever.value match {
    case Some(Success(value)) => println(s"Finished successfully with $value")
    case Some(Failure(exception)) => println(s"Failed with $exception")
    case None => println("Still running")
  }

}

trait FishingFutureExample {
  import Effects._

  trait plain {
    val buyBait: String => Bait
    val makeBait: String => Bait
    val castLine: Bait => Line
    val hookFish: Line => Fish
    def goFishing(bestBaitForFish: Future[String]): Future[Fish] =
      bestBaitForFish.map(buyBait).map(castLine).map(hookFish)
  }

  trait flat {
    val buyBait: String => Future[Bait]
    val makeBait: String => Future[Bait]
    val castLine: Bait => Future[Line]
    val hookFish: Line => Future[Fish]

    def goFishing(bestBaitForFish: Future[String]): Future[Fish] = for {
      baitName <- bestBaitForFish
      bait <- buyBait(baitName).fallbackTo(makeBait(baitName))
      line <- castLine(bait)
      fish <- hookFish(line)
    } yield fish

  }

}

