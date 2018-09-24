package ch06

trait EitherEffect {

  type OldFormat
  type NewFormat

  def runSimulation(): Either[OldFormat, NewFormat]

  type Money
  def runPowerPlant(): Either[Long, Money]


  val right = Right(10)
  val left: Either[String, Int] = Left[String, Int]("I'm left")

  def takeOne(l: Either[String, Int]): Unit

  takeOne(left)

  val i = 100

  val either = Either.cond(i > 10, i, "i is greater then 10")

  val right1 = Right(10)
  right1.withLeft[String]

  val left1 = Left("HoHoHo").withRight[BigDecimal]


  if (either.isRight) println("Got right")
  if (either.isLeft) println("Got left")

  either match {
    case Left(value) => println(s"Got Left value $value")
    case Right(value) => println(s"Got Right value $value")
  }


  if (either.contains("boo")) println("Is Right and contains 'boo'")
  if (either.exists(_ > 10)) println("Is Right and > 10")
  if (either.forall(_ > 10)) println("Is Left or > 10")


  either.getOrElse("Default value for the left side")

  def either(i: Int): Boolean = Either.cond(i > 10, i * 10, new IllegalArgumentException("Give me more")).forall(_ < 100)
}

trait UserEitherExample {
  import Effects._

  trait plain {
    val userAccount: ((String, String)) => User
    val freeAccount: ((String, String)) => User
    val subscription: User => Subscription
    val fee: Subscription => Fee
    def feeByCreds(namePass: Either[String, (String, String)]): Either[String, Fee] = namePass.map(userAccount).map(subscription).map(fee)
  }

  trait flat {
    val userAccount: ((String, String)) => Either[String, User]
    val freeAccount: ((String, String)) => Either[String, User]
    val subscription: User => Either[String, Subscription]
    val fee: Subscription => Either[String, Fee]

    private def userByNP(np: (String, String)): Either[String, User] = {
      userAccount(np).fold(_ => freeAccount(np), Right(_))
    }

    def feeByCreds(namePass: Either[String, (String, String)]): Either[String, Fee] = for {
      np <- namePass
      acc <- userByNP(np)
      sub <- subscription(acc)
      fee <- fee(sub)
    } yield fee
  }

}

