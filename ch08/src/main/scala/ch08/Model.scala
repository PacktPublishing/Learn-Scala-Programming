package ch08

import java.time.LocalDateTime

import ch08.ModelCheck._

import scala.language.higherKinds

object Model extends App {

  final case class Fish(volume: Int,
                        weight: Int,
                        teeth: Int,
                        poisonousness: Int)

  sealed trait Eatable

  final case class FreshFish(fish: Fish)

  final case class FriedFish(weight: Int) extends Eatable

  final case class CookedFish(goodTaste: Boolean) extends Eatable

  final case class Sushi(freshness: Int) extends Eatable

  final case class FedFish(untilWhen: LocalDateTime)

  lazy val check: Fish => FreshFish = f => FreshFish(f)
  lazy val prepare: FreshFish => FriedFish = f => FriedFish(f.fish.weight)
  lazy val eat: Eatable => Unit = _ => println("Yum yum...")

  def prepareAndEat: Fish => Unit = check andThen prepare andThen eat

  val fish: Fish = fishGen.sample.get
  val freshFish = check(fish)

  def mapFunc[A, B, F[_]](as: F[A])(f: A => B)(
    implicit functor: Functor[F]): F[B] =
    functor.map(as)(f)

  import Functor._
  {
    type Bucket[S] = Option[S]
    mapFunc(optionOfFishGen.sample.get)(check)
  }
  {
    type Bucket[S] = Either[Exception, S]
    mapFunc(eitherFishGen.sample.get)(check andThen prepare)
  }
  {
    type Bucket[S] = List[S]
    mapFunc(listOfFishGen.sample.get)(prepareAndEat)
  }

  final case class FishPie(weight: Int)

  def bakePie(fish: FreshFish, potatoes: Int, milk: Float): FishPie =
    FishPie(fish.fish.weight)

  lazy val freshFishMaker: List[Fish] => List[FreshFish] =
    Functor.bucketFunctor.mapC(check)

  type Bucket[S] = List[S]

  def bucketOfFish: Bucket[Fish] = listOfFishGen.sample.get.take(3)

  {
    def bakeFish(potatoes: Int, milk: Float): FreshFish => FishPie =
      bakePie(_: FreshFish, potatoes, milk)

    val pie: Seq[FishPie] =
      mapFunc(freshFishMaker(bucketOfFish))(bakeFish(20, 0.5f))
  }

  def bakeFish: FreshFish => Int => Float => FishPie = (bakePie _).curried

  lazy val pieInProgress: List[Int => Float => FishPie] =
    mapFunc(freshFishMaker(bucketOfFish))(bakeFish)

  /*
  {
    mapFunc(pie) { (pieFactory: Int => Float => FishPie) =>
      mapFunc(potatoes) { potato =>
        mapFunc(milks) { milk =>
          pieFactory(potato)(milk)
        }
      }
    }
  }
  */

  import Applicative._
  def pie(potato: Bucket[Int], milk: Bucket[Float]) = bucketApplicative(milk)(bucketApplicative(potato)(pieInProgress))

  pie(List(10), List(2f))

}

object ModelCheck {
  import Model._
  import org.scalacheck._

  val fishGen: Gen[Fish] = for {
    weight <- Gen.posNum[Int]
    volume <- Gen.posNum[Int]
    poisonousness <- Gen.posNum[Int]
    teeth <- Gen.posNum[Int]
  } yield Fish(volume, weight, teeth, poisonousness)

  implicit val arbFish: Arbitrary[Fish] = Arbitrary(fishGen)

  val listOfFishGen: Gen[List[Fish]] = Gen.listOf(fishGen)
  val optionOfFishGen: Gen[Option[Fish]] = Gen.option(fishGen)
  val eitherFishGen: Gen[Either[Exception, Fish]] =
    Gen.option(fishGen).map(_.toRight(new NoSuchElementException))

  // implicit val arbBucketOfFish: Arbitrary[Bucket[Fish]] = Arbitrary(listOfFishGen)

}
