import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalacheck.Prop.forAll

object Assesments extends App {
  def invariant[T: Ordering: Arbitrary]: Prop =
    forAll((l: List[T]) => l.sorted.length == l.length)

  invariant[Long].check
  invariant[String].check

  def idempotent[T: Ordering: Arbitrary]: Prop =
    forAll((l: List[T]) => l.sorted.sorted == l.sorted)

  idempotent[Long].check
  idempotent[String].check

  def inductive[T: Ordering: Arbitrary]: Prop = {
    def ordered(l: List[T]): Boolean =
      (l.length < 2) ||
        (ordered(l.tail) && implicitly[Ordering[T]].lteq(l.head, l.tail.head))
    forAll((l: List[T]) => ordered(l.sorted))
  }

  inductive[Int].check
  inductive[String].check


  val genListListInt = Gen.listOf(Gen.listOf(Gen.posNum[Int]))
  genListListInt.sample

  val pairGen = for {
    uuid <- Gen.uuid
    function0 <- Gen.function0(Gen.asciiStr)
  } yield (uuid, function0)

  val mapGen = Gen.mapOf(pairGen)
}
