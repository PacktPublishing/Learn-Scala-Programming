package ch07

trait Monoid[S] extends Semigroup[S] {
  def identity: S
}

object Monoid {

  val ZeroFish = Fish(0,0,0,0)

  type Bucket[S] = List[S]

  implicit val mergeBuckets: Monoid[Bucket[Fish]] = new Monoid[Bucket[Fish]] {
    override def identity: Bucket[Fish] = List.empty[Fish]
    override def op(l: Bucket[Fish], r: Bucket[Fish]): Bucket[Fish] = l ++ r
  }

  implicit val intAddition: Monoid[Int] = new Monoid[Int] {
    override def identity: Int = 0
    override def op(l: Int, r: Int): Int = l + r
  }

  implicit val intMultiplication: Monoid[Int] = new Monoid[Int] {
    override def identity: Int = 1
    override def op(l: Int, r: Int): Int = l * r
  }

  implicit val stringConcatenation: Monoid[String] = new Monoid[String] {
    override def identity: String = ""
    override def op(l: String, r: String): String = l + r
  }

  implicit val volumeMonoid: Monoid[Fish] = new Monoid[Fish] {
    override def identity: Fish = ZeroFish
    override def op(l: Fish, r: Fish): Fish =
        if (l.volume > r.volume) l.eat(r) else r.eat(l)
  }

  implicit val weightMonoid: Monoid[Fish] = new Monoid[Fish] {
    override def identity: Fish = ZeroFish
    override def op(l: Fish, r: Fish): Fish =
      if (l.weight > r.weight) l.eat(r) else r.eat(l)
  }

  implicit val poisonMonoid: Monoid[Fish] = new Monoid[Fish] {
    override def identity: Fish = ZeroFish
    override def op(l: Fish, r: Fish): Fish =
      if (l.poisonousness > r.poisonousness) l else r
  }

  implicit val teethMonoid: Monoid[Fish] = new Monoid[Fish] {
    override def identity: Fish = ZeroFish
    override def op(l: Fish, r: Fish): Fish =
      if (l.teeth > r.teeth) l else r
  }

  implicit def surviveInTheBucket(implicit m: Monoid[Fish]): Monoid[Bucket[Fish]] = new Monoid[Bucket[Fish]] {
    override def identity: Bucket[Fish] = List.fill(100)(ZeroFish)

    override def op(l: Bucket[Fish], r: Bucket[Fish]): Bucket[Fish] = {
      val operation = (m.op _).tupled
      l zip r map operation
    }
  }

  implicit val slowPoisonMonoid: Monoid[Fish] = new Monoid[Fish] {
    override def identity: Fish = ZeroFish
    override def op(l: Fish, r: Fish): Fish = {
      Thread.sleep(1)
      if (l.poisonousness > r.poisonousness) l else r
    }
  }
}
