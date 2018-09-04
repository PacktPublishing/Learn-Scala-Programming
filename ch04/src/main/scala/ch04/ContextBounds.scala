package ch04

object ContextBounds {

  trait CanEqual[T] { def hash(t: T): Int }

  implicit val stringEqual: CanEqual[String] = new CanEqual[String] {
    def hash(in: String): Int = in.hashCode()
  }

  implicit val intEqual: CanEqual[Int] = identity _

  def equal[CA, CB](a: CA, b: CB)
                   (implicit ca: CanEqual[CA], cb: CanEqual[CB]): Boolean =
    ca.hash(a) == cb.hash(b)

  def equalBounds[CA: CanEqual, CB: CanEqual](a: CA, b: CB): Boolean = {
    val hashA = implicitly[CanEqual[CA]].hash(a)
    val hashB = implicitly[CanEqual[CB]].hash(b)
    hashA == hashB
  }

  def equalDelegate[CA: CanEqual, CB: CanEqual](a: CA, b: CB): Boolean = equal(a, b)

  equal(10, 20)
  equalBounds("10", "20")
  equalDelegate(1598, "20")
}
