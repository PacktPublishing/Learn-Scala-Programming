package ch08

import java.time.LocalDateTime

import ch08.ModelCheck._

import scala.language.{higherKinds, reflectiveCalls}
import scala.util.{Random, Success, Try}

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
    def bakeFishAtOnce(potatoes: Int, milk: Float): FreshFish => FishPie =
      bakePie(_: FreshFish, potatoes, milk)

    val pie: Seq[FishPie] =
      mapFunc(freshFishMaker(bucketOfFish))(bakeFishAtOnce(20, 0.5f))
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

  def pie3[F[_]: Applicative](fish: F[FreshFish], potato: F[Int], milk: F[Float]): F[FishPie] =
    implicitly[Applicative[F]].map3(fish, potato, milk)(bakePie)

  def checkHonestly[F[_] : Applicative](noFish: F[FreshFish])(fish: Fish): F[FreshFish] =
    if (Random.nextInt(3) == 0) noFish else implicitly[Applicative[F]].unit(FreshFish(fish))

  def genericPie3[O[_], B[_]](fish: B[O[FreshFish]], potato: B[O[Int]], milk: B[O[Float]])(implicit a:Applicative[({type BO[x] = B[O[x]]})#BO]): B[O[FishPie]] = {
    a.map3(fish, potato, milk)(bakePie)
  }

  val trueFreshFish: List[Option[FreshFish]] = bucketOfFish.map(checkHonestly(Option.empty[FreshFish]))
  def freshPotato(count: Int) = List(Some(count))
  def freshMilk(gallons: Float) = List(Some(gallons))

  implicit val bucketOfFresh: Applicative[({ type T[x] = Bucket[Option[x]]})#T] =
    bucketApplicative.compose(optionApplicative)

  val freshPie = pie3[({ type T[x] = Bucket[Option[x]]})#T](trueFreshFish, freshPotato(10), freshMilk(0.2f))

  println(freshPie)

  println(Traversable.bucketTraversable.sequence(freshPie))

  val allOrNothing = Traversable.bucketTraversable.traverse(bucketOfFish) { a: Fish =>
    checkHonestly(Option.empty[FreshFish])(a).map(f => bakePie(f, 10, 0.2f))
  }

  println(allOrNothing)


  def deep[X](x: X) = Success(Right(x))

  type DEEP[x] = Try[Either[Unit, Bucket[Option[x]]]]

  implicit val deepBucket: Applicative[DEEP] =
    tryApplicative.compose(eitherApplicative[Unit].compose(bucketApplicative.compose(optionApplicative)))

  val deeplyPackaged =
    pie3[DEEP](deep(trueFreshFish), deep(freshPotato(10)), deep(freshMilk(0.2f)))

  println(deeplyPackaged)

  import Traversable._
  val deepTraverse = tryTraversable.compose(eitherTraversable[Unit].compose(bucketTraversable))

  val deepYummi = deepTraverse.traverse(deeplyPackaged) { pie: Option[FishPie] =>
    pie.foreach(p => println(s"Yummi $p"))
    pie
  }
  println(deepYummi)
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
