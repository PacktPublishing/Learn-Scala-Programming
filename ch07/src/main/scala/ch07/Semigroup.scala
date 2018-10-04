package ch07

final case class Fish(volume: Int, weight: Int, teeth: Int, poisonousness: Int) {
  def eat(f: Fish): Fish = Fish(volume + f.volume, weight + f.weight, teeth + f.teeth, poisonousness + f.poisonousness)
}

trait Semigroup[S] {
  def op(l: S, r: S): S
}

object Semigroup {

  implicit val volumeSemigroup: Semigroup[Fish] = (l: Fish, r: Fish) =>
    if (l.volume > r.volume) l.eat(r) else r.eat(l)

  implicit val weightSemigroup: Semigroup[Fish] = (l: Fish, r: Fish) =>
    if (l.weight > r.weight) l.eat(r) else r.eat(l)

  implicit val poisonSemigroup: Semigroup[Fish] = (l: Fish, r: Fish) =>
    if (l.poisonousness > r.poisonousness) l else r

  implicit val teethSemigroup: Semigroup[Fish] = (l: Fish, r: Fish) =>
    if (l.teeth > r.teeth) l else r

  implicit val intAddition: Semigroup[Int] = (l: Int, r: Int) => l + r

  implicit val intMultiplication: Semigroup[Int] = (l: Int, r: Int) => l * r

  implicit val stringConcatenation: Semigroup[String] = (l: String, r: String) => l + r
}