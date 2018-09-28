package ch07

import ch07.Monoid.Bucket
import ch07.SemigroupSpecification.{associativity, fishGen}
import org.scalacheck.Prop._
import org.scalacheck._

object MonoidSpecification extends Properties("Monoid") {

  def identity[S : Monoid : Arbitrary]: Prop =
    forAll((a: S) => {
      val m = implicitly[Monoid[S]]
      m.op(a, m.identity) == a && m.op(m.identity, a) == a
    })


  def monoidProp[S : Monoid : Arbitrary]: Prop = associativity[S] && identity[S]

  val bucketOfFishGen: Gen[List[Fish]] = Gen.listOf(fishGen)
  implicit val arbBucketOfFish: Arbitrary[Bucket[Fish]] = Arbitrary(bucketOfFishGen)

  import SemigroupSpecification._

  property("bucket of fish monoid") = {
    import Monoid.mergeBuckets
    monoidProp[Bucket[Fish]]
  }

  property("ints under addition") = {
    import Monoid.intAddition
    monoidProp[Int]
  }

  property("ints under multiplication") = {
    import Monoid.intMultiplication
    monoidProp[Int]
  }

  property("strings under concatenation") = {
    import Monoid.stringConcatenation
    monoidProp[String]
  }

  property("props for fish monoid under 'big eats little'") = {
    import Monoid.volumeMonoid
    monoidProp[Fish]
  }

  property("props for fish monoid under 'heavy eats light'") = {
    import Monoid.weightMonoid
    monoidProp[Fish]
  }

  property("props for fish monoid under 'poisonous drives away others'") = {
    import Monoid.poisonMonoid
    monoidProp[Fish]
  }
  property("props for fish monoid under 'teeth FTW'") = {
    import Monoid.teethMonoid
    monoidProp[Fish]
  }

  property("props for survival in the bucket for most poisonousness") = {
    import Monoid.poisonMonoid
    import Monoid.surviveInTheBucket
    monoidProp[Bucket[Fish]]
  }

  property("props for survival in the bucket for heaviest") = {
    import Monoid.weightMonoid
    import Monoid.surviveInTheBucket
    monoidProp[Bucket[Fish]]
  }

}
