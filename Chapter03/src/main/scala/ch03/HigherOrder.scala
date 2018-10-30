package ch03

object HigherOrder {
  def printHash(hasher: String => Int)(s: String): Unit = println(hasher(s))
  def printPoly[A](hasher: A => Int)(s: A): Unit = println(hasher(s))
  def printer[A, B, C <: A](a: C)(f: A => B): Unit = println(f(a))
  printPoly((_: String).hashCode)("HaHa")
  printer(42)((_: Int) / 2)
  printer("HoHo")(_.length)
  printer(42)(identity)
}
