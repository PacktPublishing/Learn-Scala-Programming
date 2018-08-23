package ch02


object TypeConstraints {

  trait A

  trait B extends A

  trait C extends B

  trait bounds {
    type LOWER >: B
    type UPPER <: B
  }

  trait boundsA extends bounds {
    override type LOWER = A
    // override type UPPER = A // compile error
  }

  trait boundsB extends bounds {
    override type LOWER = B
    override type UPPER = B
  }

  trait boundsC extends bounds {
    // override type LOWER = C // compile error
    override type UPPER = C
  }

}
