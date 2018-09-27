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


  def monoidProps[S : Monoid : Arbitrary]: Prop = associativity[S] && identity[S]

  val bucketOfFishGen: Gen[List[Fish]] = Gen.listOf(fishGen)
  implicit val arbBucketOfFish: Arbitrary[Bucket[Fish]] = Arbitrary(bucketOfFishGen)

  import SemigroupSpecification._

  property("bucket of fish monoid") = {
    import Monoid.mergeBuckets
    monoidProps[Bucket[Fish]]
  }

  property("ints under addition") = {
    import Monoid.intAddition
    monoidProps[Int]
  }

  property("ints under multiplication") = {
    import Monoid.intMultiplication
    monoidProps[Int]
  }

  property("strings under concatenation") = {
    import Monoid.stringConcatenation
    monoidProps[String]
  }

  property("props for fish monoid under 'big eats little'") = {
    import Monoid.volumeMonoid
    monoidProps[Fish]
  }

  property("props for fish monoid under 'heavy eats light'") = {
    import Monoid.weightMonoid
    monoidProps[Fish]
  }

  property("props for fish monoid under 'poisonous drives away others'") = {
    import Monoid.poisonMonoid
    monoidProps[Fish]
  }
  property("props for fish monoid under 'teeth FTW'") = {
    import Monoid.teethMonoid
    monoidProps[Fish]
  }

  property("props for survival in the bucket for most poisonousness") = {
    import Monoid.poisonMonoid
    import Monoid.surviveInTheBucket
    monoidProps[Bucket[Fish]]
  }

  property("props for survival in the bucket for heaviest") = {
    import Monoid.weightMonoid
    import Monoid.surviveInTheBucket
    monoidProps[Bucket[Fish]]
  }

  val bucket = bucketOfFishGen.sample.get
  bucket.reduce(Semigroup.poisonSemigroup.op)

  List.empty[Fish].reduce(Semigroup.poisonSemigroup.op)
}
