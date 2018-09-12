import org.scalacheck.{Arbitrary, Gen, Shrink}
import org.scalacheck.Prop._

object Shrinking {

  forAll { (_: Int) < 42 }.check

  forAll(Gen.listOfN(1000, Arbitrary.arbString.arbitrary)) {
    _.forall(_.length < 10)
  }.check


  val intShrink: Shrink[Int] = implicitly[Shrink[Int]]

  intShrink.shrink(2008612603).toList

}
