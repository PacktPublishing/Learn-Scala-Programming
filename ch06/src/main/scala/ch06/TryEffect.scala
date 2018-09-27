package ch06

import java.io.IOError

import scala.util._
import scala.io.StdIn.readLine

trait TryEffect {
  val success = Success("Well")
  val failure = Failure(new Exception("Not so well"))

  val firstTry = try {
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

trait UserTryExample {
  import Effects._

  trait plain {
    val userAccount: ((String, String)) => User
    val freeAccount: ((String, String)) => User
    val subscription: User => Subscription
    val fee: Subscription => Fee
    def feeByCreds(namePass: Try[(String, String)]): Try[Fee] = namePass.map(userAccount).map(subscription).map(fee)
  }

  trait flat {
    val userAccount: ((String, String)) => Try[User]
    val freeAccount: ((String, String)) => Try[User]
    val subscription: User => Try[Subscription]
    val fee: Subscription => Try[Fee]

    def feeByCreds(namePass: Try[(String, String)]): Try[Fee] = for {
      np <- namePass
      acc <- userAccount(np).fold(_ => freeAccount(np), Success(_))
      sub <- subscription(acc)
      fee <- fee(sub)
    } yield fee
  }

}

