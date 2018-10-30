package ch06

object OptionEffect extends App {
  val opt0 = None
  val opt: Option[Int] = Some(10)
  val opt2 = Some("I'm a non-empty option")
  val opt3 = Some(null)

  def opt4[A](a: A): Option[A] = if (a == null) None else Some(a)

  def opt5[A](a: A): Option[A] = Option(a)

  if (opt.isDefined) println(opt.get)
  if (opt.isEmpty) println("Ooops") else println(opt.get)

  if (opt.contains("boo")) println("Opt is non-empty and contains 'boo'")
  if (opt.exists(_ > 10)) println("Opt is non-empty and > 10")
  if (opt.forall(_ > 10)) println("Opt is empty or  > 10")

  if (opt.isDefined) {
    val Some(value) = opt
  }

  opt match {
    case Some(value) => println(value)
    case None        => println("no value")
  }

  opt.getOrElse("No value")

  opt2.orNull

  opt2.foreach(println)

  val opt5 = opt0 orElse opt2 orElse opt3

  val moreThen10: Option[Int] = opt.filter(_ > 10)
  val lessOrEqual10: Option[Int] = opt.filterNot(_ > 10)

  val moreThen20: Option[String] = opt.collect {
    case i if i > 20 => s"More then 20: $i"
  }

  opt.toRight("If opt is empty, I'll be Left[String]")

  opt.toLeft("Nonempty opt will be Left, empty - Right[String]")

  opt.fold("Value for an empty case")((i: Int) => s"The value is $i")
}

trait FishingOptionExample {

  import Effects._
  trait plain {
    val buyBait: String => Bait
    val makeBait: String => Bait
    val castLine: Bait => Line
    val hookFish: Line => Fish

    def goFishing(bestBaitForFish: Option[String]): Option[Fish] =
      bestBaitForFish.map(buyBait).map(castLine).map(hookFish)
  }

  trait flat {
    val buyBait: String => Option[Bait]
    val makeBait: String => Option[Bait]
    val castLine: Bait => Option[Line]
    val hookFish: Line => Option[Fish]

    def goFishingOld(bestBaitForFish: Option[String]): Option[Fish] =
      bestBaitForFish.flatMap(buyBait).flatMap(castLine).flatMap(hookFish)

    def goFishing(bestBaitForFish: Option[String]): Option[Fish] =
      for {
        baitName <- bestBaitForFish
        bait <- buyBait(baitName).orElse(makeBait(baitName))
        line <- castLine(bait)
        fish <- hookFish(line)
      } yield fish
  }

}
