package ch04

object ImplicitConversions {
  import scala.language.implicitConversions

  // defined in predef
  // implicit def int2Integer(x: Int): java.lang.Integer = x.asInstanceOf[java.lang.Integer]
  // implicit def Integer2int(x: java.lang.Integer): Int = x.asInstanceOf[Int]

  val integer: Integer = RandomInt.randomInt()
  val int: Int = math.abs(integer)

  // @inline implicit def augmentString(x: String): StringOps = new StringOps(x)

  "I'm a string".flatMap(_.toString * 2) ++ ", look what I can"

  case class A[T](a: T)
  case class B[T](a: T)

  implicit def t2A[T](a: T): A[T] = A(a)
  implicit def t2B[T](a: T): B[T] = B(a)

  def ab[C](a: B[A[C]]): Unit = println(a)

  ab(A("A"))

  // ab("A") fails

  ab("A": A[String])

  implicit val directions: List[String] = List("North", "West", "South", "East")
  implicit val grades: Map[Char, String] =
    Map('A' -> "90%", 'B' -> "80%", 'C' -> "70%", 'D' -> "60%", 'F' -> "0%")

  println("B" + 42: String)
  println("B" + (42: String))
  "B" + 'C'
  "B" + ('C': String)
  "B" + (2: String)
}
