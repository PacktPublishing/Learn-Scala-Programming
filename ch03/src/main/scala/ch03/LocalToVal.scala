package ch03

object LocalToVal {
  val items = Seq(1, 2, 3, 4, 5)
  val avg = {
    def sum(in: Int*): Int = in.sum

    def count(in: Int*): Int = in.size

    sum(items: _*) / count(items: _*)
  }
}
