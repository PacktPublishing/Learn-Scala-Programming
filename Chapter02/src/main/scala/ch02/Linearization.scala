package ch02

object Linearization {
  trait A {
    override def toString: String = super.toString + "A"
  }

  trait B {
    override def toString: String = super.toString + "B"
  }

  trait C {
    override def toString: String = super.toString + "C"
  }

  class E extends A with B with C {
    override def toString: String = super.toString + "D"
  }
}



