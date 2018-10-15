package ch09

object Ch09 extends App {

  var globalState = 0

  def incGlobal(count: Int): Int = {
    globalState += count
    globalState
  }

  val g1 = incGlobal(10) // g1 == 10
  val g2 = incGlobal(10) // g1 == 20

  println(g1, g2)

  def incLocal(count: Int, global: Int): Int = global + count

  val l1 = incLocal(10, 0) // l1 = 10
  val l2 = incLocal(10, 0) // l2 = 10


  println(l1, l2)
}
