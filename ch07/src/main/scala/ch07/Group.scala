package ch07

trait Group[S] extends Monoid[S] {
  def inverse(a: S): S
}

object Group {

  implicit val intAddition: Group[Int] = new Group[Int] {
    override def identity: Int = 0
    override def op(l: Int, r: Int): Int = l + r
    override def inverse(a: Int): Int = identity - a
  }

}
