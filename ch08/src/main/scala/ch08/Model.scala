package ch08

import java.time.LocalDateTime

import ch08.Model._
import ch08.Check._

object Model {

  final case class Fish(volume: Int, weight: Int, teeth: Int, poisonousness: Int)

  sealed trait Eatable

  final case class FreshFish(fish: Fish)
  final case class FriedFish(weight: Int) extends Eatable
  final case class CookedFish(goodTaste: Boolean) extends Eatable
  final case class Sushi(freshness: Int) extends Eatable
  final case class FedFish(untilWhen: LocalDateTime)

  val check: Fish => FreshFish = f => FreshFish(f)
  val prepare: FreshFish => FriedFish = f => FriedFish(f.fish.weight)
  val eat: Eatable => Unit = _ => println("Yum yum...")

  def prepareAndEat: Fish => Unit = check andThen prepare andThen eat

  val fish: Fish = fishGen.sample.get
  val freshFish = check(fish)
}

object Check {
  import org.scalacheck._
  val fishGen: Gen[Fish] = for {
    weight <- Gen.posNum[Int]
    volume <- Gen.posNum[Int]
    poisonousness <- Gen.posNum[Int]
    teeth <- Gen.posNum[Int]
  } yield Fish(volume, weight, teeth, poisonousness)

  implicit val arbFish: Arbitrary[Fish] = Arbitrary(fishGen)
}
