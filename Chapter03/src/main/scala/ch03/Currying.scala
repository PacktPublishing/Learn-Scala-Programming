package ch03

object Currying {
  def sum(a: Int, b: Int) = a + b
  def sumAB(a: Int)(b: Int) = a + b
  val sum6 = (a: Int) => (b: Int) => (c: Int) => (d: Int) => (e: Int) => (f: Int) => a + b + c + d+ e + f
  val sum6Placeholder = (_: Int) + (_: Int) + (_: Int) + (_: Int) + (_: Int) + (_: Int)
}
