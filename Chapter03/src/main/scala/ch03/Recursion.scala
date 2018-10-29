package ch03

object Recursion extends App {

  def reverse(s: String): String = {
    if (s.length < 2) s
    else reverse(s.tail) + s.head
  }

  println(reverse("Recursive function call"))

  def tailRecReverse(s: String): String = {
    @scala.annotation.tailrec
    def reverse(s: String, acc: String): String =
      if (s.length < 2) s + acc
      else reverse(s.tail, s.head + acc)

    reverse(s, "")
  }

  def inspectReverse(s: String): String = {
    @scala.annotation.tailrec
    def reverse(s: String, acc: String): String =
      if (s.length < 2) {
        new Exception().printStackTrace(); s + acc
      }
      else reverse(s.tail, s.head + acc)

    reverse(s, "")
  }

  println(inspectReverse("Recursive function call"))

  object Hofstadter {
    def F(n: Int): Int = if (n == 0) 1 else n - M(F(n - 1))

    def M(n: Int): Int = if (n == 0) 0 else n - F(M(n - 1))
  }

  println(Hofstadter.F(100))

  val A: (Long, Long) => Long = (m, n) =>
    if (m == 0) n + 1
    else if (n == 0) A(m - 1, 1)
    else A(m - 1, A(m, n - 1))

  object Trampolined {
    import util.control.TailCalls._

    def tailA(m: BigInt, n: BigInt): TailRec[BigInt] = {
      if (m == 0) done(n + 1)
      else if (n == 0) tailcall(tailA(m - 1, 1))
      else tailcall(tailA(m, n - 1)).flatMap(tailA(m - 1, _))
    }
    def A(m: Int, n: Int): BigInt = tailA(m, n).result
  }
}

