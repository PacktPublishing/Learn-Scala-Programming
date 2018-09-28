package ch07

import org.scalacheck.Prop._
import org.scalacheck._

object GroupSpecification extends Properties("Group") {

  import MonoidSpecification._

  def invertibility[S : Group : Arbitrary]: Prop =
    forAll((a: S) => {
      val m = implicitly[Group[S]]
      m.op(a, m.inverse(a)) == m.identity && m.op(m.inverse(a), a) == m.identity
    })

  def groupProp[S : Group: Arbitrary]: Prop = monoidProp[S] && invertibility[S]

  def commutativity[S : Group : Arbitrary]: Prop =
    forAll((a: S, b: S) => {
      val m = implicitly[Group[S]]
      m.op(a, b) == m.op(b, a)
    })

  def abelianGroupProp[S : Group: Arbitrary]: Prop = groupProp[S] && commutativity[S]

  property("ints under addition form a group") = {
    import Group.intAddition
    groupProp[Int]
  }

  property("ints under addition form an abelian group") = {
    import Group.intAddition
    groupProp[Int]
  }

}
