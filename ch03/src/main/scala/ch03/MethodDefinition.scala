package ch03

object MethodDefinition {
  def equal(arg1: String, arg2: Int): Boolean = !nonEqual(arg1, arg2)

  private def nonEqual(arg1: String, arg2: Int) = arg1 != arg2.toString

  def defaultValues(a: String = "default")(b: Int = 0, c: String = a)(
      implicit d: Long = b,
      e: String = a) = ???

  def byName(int: => Int) = {
    println(int)
    println(int)
  }

  byName({
    println("Calculating")
    10 * 2
  })

  def variable(a: String, b: Int*): Unit = {
    val _: collection.Seq[Int] = b
  }

  variable("vararg", 1, 2, 3)
  variable("Seq", Seq(1, 2, 3): _*)

  def named(first: Int, second: String, third: Boolean) =
    s"$first, $second, $third"

  named(third = false, first = 10, second = "Nice")
  named(10, third = true, second = "Cool")
}
