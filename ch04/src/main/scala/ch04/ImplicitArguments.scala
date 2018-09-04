package ch04

object ImplicitArguments {

  case class A[T](a: T)
  case class B[T](a: T)

  def ab[C](name: String)(a: A[C])(implicit b: B[C]): Unit = println(s"$name$a$b")

  // ab("1")(A("A")) // fails to compile

  implicit val b: B[String] = B("[Implicit]")

  ab("1")(A("A"))

  // implicit val c: B[String] = B("[Another Implicit]")

  // ab("1")(A("A")) // fails to compile

  ab("1")(A("A"))(b)
  // ab("1")(A("A"))(c)

  def withTimestamp(s: String)(implicit time: Long): Unit = println(s"$time: $s")

  object scope1 {
    implicit def randomLong: Long = scala.util.Random.nextLong()

    withTimestamp("First")
    withTimestamp("Second")
  }

  object scope2 {
    implicit def recursiveLong(implicit seed: Long): Long = scala.util.Random.nextLong(seed)

//    withTimestamp("Third") // fails to compile
  }


  object Application {
    case class Configuration(name: String)
    implicit val cfg: Configuration = Configuration("test")
    class Persistence(implicit cfg: Configuration) {
      class Database(implicit cfg: Configuration) {
        def query(id: Long)(implicit cfg: Configuration) = ???
        def update(id: Long, name: String)(implicit cfg: Configuration) = ???
      }
      new Database().query(1L)
    }
  }

}
