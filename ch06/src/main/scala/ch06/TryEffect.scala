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
    val buyBait: String => Bait
    val makeBait: String => Bait
    val castLine: Bait => Line
    val hookFish: Line => Fish
    def goFishing(bestBaitForFishOrCurse: Try[String]): Try[Fish] =
      bestBaitForFishOrCurse.map(buyBait).map(castLine).map(hookFish)
  }

  trait flat {
    val buyBait: String => Try[Bait]
    val makeBait: String => Try[Bait]
    val castLine: Bait => Try[Line]
    val hookFish: Line => Try[Fish]

    def goFishing(bestBaitForFishOrCurse: Try[String]): Try[Fish] = for {
      baitName <- bestBaitForFishOrCurse
      bait <- buyBait(baitName).fold(_ => makeBait(baitName), Success(_))
      line <- castLine(bait)
      fish <- hookFish(line)
    } yield fish
  }

}

