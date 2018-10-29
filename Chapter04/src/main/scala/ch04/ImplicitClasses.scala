package ch04


object ImplicitClasses {

  case class A[T](a: T) { def doA(): T = a }

  case class B[T](b: T) { def doB(): T = b }

  import scala.language.implicitConversions
  implicit def a2b[T](a: A[T]): B[T] = B(a.a)

  A("I'm an A").doB()


  implicit class C[T](a: A[T]) { def doC(): T = a.a }

  A("I'm an A").doC()
}
