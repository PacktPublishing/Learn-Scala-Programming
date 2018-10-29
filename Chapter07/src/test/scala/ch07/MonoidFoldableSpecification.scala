package ch07

import ch07.Monoid.Bucket
import ch07.SemigroupSpecification.fishGen
import org.scalacheck.Prop._
import org.scalacheck.Test.Parameters
import org.scalacheck._

import scala.concurrent.Await

object MonoidFoldableSpecification extends Properties("MonoidFoldable") {
  implicit val params: Parameters = Parameters.default.withMinSuccessfulTests(10)

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val bucketOfFishGen: Gen[List[Fish]] = for {
    n <- Gen.choose(100, 1000)
    gen <- Gen.listOfN(n, fishGen)
  } yield gen

  implicit val arbBucketOfFish: Arbitrary[Bucket[Fish]] = Arbitrary(bucketOfFishGen)

  def withTime(block: => Fish): (Fish, Long) = {
    val start = System.nanoTime()
    val result = block
    (result, (System.nanoTime() - start) / 1000000)
  }

  property("foldPar is the quickest way to fold a list") = {
    import Monoid.slowPoisonMonoid
    val foldable = MonoidFoldable.listMonoidFoldable[Fish]

    forAllNoShrink((as: Bucket[Fish]) => {
      val (left, leftRuntime) = withTime(foldable.foldLeft(as))
      val (right, rightRuntime) = withTime(foldable.foldRight(as))
      val (balanced, balancedRuntime) = withTime(foldable.foldBalanced(as))
      val (parallel, parallelRuntime) = withTime(Await.result(foldable.foldPar(as), 5.seconds))

      s"${as.size} fishes: $leftRuntime, $rightRuntime, $balancedRuntime, $parallelRuntime millis" |: all(
        "all results are equal" |: all(left == right, left == balanced, left == parallel),
        "parallel is quickest" |: parallelRuntime <= List(leftRuntime, rightRuntime, balancedRuntime).min
      )
    })
  }

}
