package ch06

import java.io.IOError

import scala.util._
import scala.io.StdIn.readLine

trait TryEffect {
  val success = Success("Well")
  val failure = Failure(new Exception("Not so well"))

  val firstTry: Try[String] = try {
    Success(readLine())
  } catch {
    case err: IOError => Failure(err)
  }

  val secondTry = Try(readLine())

  val line = Try {
    val line = readLine()
    println(s"Got $line from console")
    line
  }

  if (line.isSuccess) println(s"The line was ${line.get}")
  if (line.isFailure) println(s"There was a failure")

  line.filter(_.nonEmpty)

  line.collect {  case s: String => s * 10 }

  def retryAfterDelay = ???

  line.recover {
    case ex: NumberFormatException => Math.PI
  }

  line.recoverWith {
    case ex: NoSuchElementException => Try(retryAfterDelay)
  }

  val result = firstTry orElse secondTry orElse failure orElse success

}

trait FishingTryExample {
  import Effects._

  trait plain {
    val buyBate: String => Bate
    val makeBate: String => Bate
    val castLine: Bate => Line
    val hookFish: Line => Fish
    def goFishing(bestBateForFishOrCurse: Try[String]): Try[Fish] =
      bestBateForFishOrCurse.map(buyBate).map(castLine).map(hookFish)
  }

  trait flat {
    val buyBate: String => Try[Bate]
    val makeBate: String => Try[Bate]
    val castLine: Bate => Try[Line]
    val hookFish: Line => Try[Fish]

    def goFishing(bestBateForFishOrCurse: Try[String]): Try[Fish] = for {
      bateName <- bestBateForFishOrCurse
      bate <- buyBate(bateName).fold(_ => makeBate(bateName), Success(_))
      line <- castLine(bate)
      fish <- hookFish(line)
    } yield fish
  }

}

