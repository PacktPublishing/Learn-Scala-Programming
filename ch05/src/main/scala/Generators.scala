import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Gen, Prop}

object Generators {

  val morgans: Prop = forAll { (a: Boolean, b: Boolean) =>
    collect(s"$a $b")(!(a & b) == (!a | !b))
  }
  morgans.check


  def literalGen[T <: Singleton](t: T): Gen[T] = Gen.const(t)

  implicit val myGen: Arbitrary[42] = Arbitrary(literalGen(42))

  val literalProp: Prop = forAll((_: 42) == 42)

  literalProp.check

  sealed trait Rank
  case class SymRank(s: Char) extends Rank {
    override def toString: String = s.toString
  }
  case class NumRank(n: Int) extends Rank {
    override def toString: String = n.toString
  }
  case class Card(suit: Char, rank: Rank) {
    override def toString: String = s"$suit $rank"
  }

  val suits: Gen[Char] = Gen.oneOf('♡', '♢', '♤', '♧')
  val numbers: Gen[NumRank] = Gen.choose(2, 10).map(NumRank)
  val symbols: Gen[SymRank] = Gen.oneOf('A', 'K', 'Q', 'J').map(SymRank)

  val full: Gen[Card] = for {
    suit <- suits
    rank <- Gen.frequency((9, numbers), (4, symbols))
  } yield Card(suit, rank)

  val piquetBad: Gen[Card] = full.suchThat {
    case Card(_, _: SymRank) => true
    case Card(_, NumRank(n)) => n > 5
  }


  forAll(full) { card =>
    Prop.collect(card)(true)
  }.check

  val piquetNumbers: Gen[NumRank] = Gen.choose(6, 10).map(NumRank)

  val piquet: Gen[Card] = for {
    suit <- suits
    rank <- Gen.frequency((5, piquetNumbers), (4, symbols))
  } yield Card(suit, rank)

  forAll(piquet) { card =>
    Prop.collect(card)(true)
  }.check


  val handOfCards1: Gen[List[Card]] = Gen.listOfN(6, piquet)

  val handOfCards: Gen[Set[Card]] = Gen.containerOfN[Set, Card](6, piquet).retryUntil(_.size == 6)

  forAll(handOfCards) { hand =>
    Prop.collect(hand.mkString(","))(true)
  }.check

}
