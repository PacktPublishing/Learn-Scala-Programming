package ch02

object GeneralisedConstraints {
  import Linearization._
  abstract class Wrapper[A] {
    val a: A

    // A in flatten shadows A in Wrapper
    // def flatten[B, A <: Wrapper[B]]: Wrapper[B] = a
    def flatten(implicit ev: A <:< Wrapper[B]): Wrapper[B] = a
  }

}
