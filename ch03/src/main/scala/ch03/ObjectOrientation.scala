package ch03


object ObjectOrientation {
  val A: Function2[Long, Long, Long] = (m, n) =>
    if (m == 0) n + 1
    else if (n == 0) A.apply(m - 1, 1)
    else A.apply(m - 1, A.apply(m, n - 1))

  val objectOrientedA: Function2[Long, Long, Long] = new Function2[Long, Long, Long] {
    def apply(m: Long, n: Long): Long =
      if (m == 0) n + 1
      else if (n == 0) objectOrientedA(m - 1, 1)
      else objectOrientedA(m - 1, objectOrientedA(m, n - 1))
  }

  def isPalindrome(s: String): Boolean = {
    @scala.annotation.tailrec
    def helper(s: String, acc: Boolean):Boolean = {
      if (!acc) acc
      else if (s.length < 2) true else helper(s.drop(1).dropRight(1), s.head == s.last)
    }
    helper(s, acc = true)
  }

  val doReverse: PartialFunction[String, String] = {
    case str if !isPalindrome(str) => str.reverse
  }
  val noReverse: PartialFunction[String, String] = {
    case str if isPalindrome(str) => str
  }
  def reverse = noReverse orElse doReverse


  val upper = (_: String).toUpperCase
  def fill(c: Char) = c.toString * (_: String).length
  def filter(c: Char) = (_: String).filter(_ == c)

  val chain = List(upper, filter('L'), fill('*'))
  val allAtOnce = Function.chain(chain)

  val static = upper andThen filter('a') andThen fill('C')

  allAtOnce("List(upper _, filter('a'), fill('C'))")
}
