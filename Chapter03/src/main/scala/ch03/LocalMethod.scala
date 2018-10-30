package ch03

object LocalMethod {
  def average(in: Int*): Int = {
    def sum(in: Int*): Int = in.sum

    def count(in: Int*): Int = in.size

    sum(in: _*) / count(in: _*)
  }

  val items = Seq(1, 2, 3, 4, 5)
  val avg = average(items: _*)

  def averageNoPassing(in: Int*): Int = {
    def sum: Int = in.sum

    def count: Int = in.size

    sum / count
  }
}
