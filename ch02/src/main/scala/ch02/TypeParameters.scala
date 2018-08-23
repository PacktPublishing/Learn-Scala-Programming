package ch02

import java.io


object TypeParameters {

  case class Wrapper[A](content: A) {
    def unwrap: A = content
  }

  def createWrapper[A](a: A): Wrapper[A] = Wrapper(a)

  type ConcreteWrapper[A] = Wrapper[A]

  val wInt: Wrapper[Int] = createWrapper[Int](10)
  val wLong: ConcreteWrapper[Long] = createWrapper(10L)
  val int: Int = wInt.unwrap
  val long: Long = wLong.unwrap

  import scala.language.higherKinds

  type AbstractWrapper[A]

  case class Abc[A](a: A, b: A, c: A)

  val intA: Abc[Int] = Abc(10, 20, 30)
  val longA: Abc[Long] = Abc(10L, 20L, 30L)
  val whatA: Abc[AnyVal] = Abc(10, 20, true)
  val whatB: Abc[io.Serializable] = Abc("10", "20", Wrapper(10))
  val whatC: Abc[Any] = Abc(10, "20", Wrapper(10))

  trait Constraints[A <: AnyVal, B >: Null <: AnyRef] {
    def a: A

    def b: B
  }

  // compile error - type parameter bounds
  // case class AB(a: String, b: Int) extends Constraints[String, Int]

  // case class AB(a: Int, b: String) extends Constraints[Int, String]
}
