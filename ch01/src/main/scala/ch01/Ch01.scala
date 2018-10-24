package ch01

import java.util.concurrent.atomic.AtomicLong
import scala.io.StdIn

object Ch01 extends App {

  "10".toIntOption
  "TrUe".toBooleanOption

  val bool = "Not True"
  bool.toBooleanOption

  scala.util.Using

  val user = User("John", "Doe", "jd@mail.me")
  user.productElementNames.mkString(", ")
  user.productElementName(3)

  val tuple = (1, "two", false)

  tuple.productElementNames.mkString(", ")
  tuple.productElementName(1)

  def naiveToJsonString(p: Product): String =
    (for { i <- 0 until p.productArity } yield
      s""""${p.productElementName(i)}": "${p.productElement(i)}"""")
      .mkString("{ ", ", ", " }")

  naiveToJsonString(user)


  import scala.util.chaining._



  import UserDb._
  val userId = 1L
  save(update(getById(userId)))

  getById(userId).pipe(update).pipe(save)

  val getByIdUpdateAndSave = (getById _).andThen(update).andThen(save)
  getByIdUpdateAndSave(userId)


  val lastTick = new AtomicLong(0)
  def start(): Unit = lastTick.set(System.currentTimeMillis())
  def measure[A](a: A): Unit = {
    val now = System.currentTimeMillis()
    val before = lastTick.getAndSet(now)
    println(s"$a: ${now-before} ms elapsed")
  }

  start()
  val result = StdIn.readLine().pipe(_.toIntOption).tap(measure)
  val anotherResult = StdIn.readLine().pipe(_.toIntOption).tap(measure)

  

}
case class User(name: String, surname: String, email: String)

object UserDb {
  def getById(id: Long): User = ???
  def update(u: User): User = ???
  def save(u: User): Boolean = ???
}
