package ch07

import org.scalacheck._
import org.scalacheck.Prop._

object SemigroupSpecification extends Properties("Semigroup") {

  def associativity[S : Semigroup : Arbitrary]: Prop =
    forAll((a: S, b: S, c: S) => {
      val sg = implicitly[Semigroup[S]]
      sg.op(sg.op(a, b), c) == sg.op(a, sg.op(b, c))
    })

  val fishGen: Gen[Fish] = for {
    weight <- Gen.posNum[Int]
    volume <- Gen.posNum[Int]
    poisonousness <- Gen.posNum[Int]
    teeth <- Gen.posNum[Int]
  } yield Fish(volume, weight, teeth, poisonousness)

  implicit val arbFish: Arbitrary[Fish] = Arbitrary(fishGen)

  property("associativity for fish semigroup under 'big eats little'") = {
    import Semigroup.volumeSemigroup
    associativity[Fish]
  }

  property("associativity for fish semigroup under 'heavy eats light'") = {
    import Semigroup.weightSemigroup
    associativity[Fish]
  }

  property("associativity for fish semigroup under 'poisonous drives away others'") = {
    import Semigroup.poisonSemigroup
    associativity[Fish]
  }
  property("associativity for fish semigroup under 'teeth FTW'") = {
    import Semigroup.teethSemigroup
    associativity[Fish]
  }

  property("associativity for int under addition") = {
    import Semigroup.intAddition
    associativity[Int]
  }
  property("associativity for int under multiplication") = {
    import Semigroup.intMultiplication
    associativity[Int]
  }
  property("associativity for strings under concatenation") = {
    import Semigroup.stringConcatenation
    associativity[String]
  }

}
