package ch02


object SelfType {

  trait A {
    def a: String
  }

  trait B {
    def b: String
  }

  trait C {
    this: A => // override `this`
    def c: String = this.a
  }

  trait D {
    self: A with B => // self is an alias for mixed traits
    def d: String = this.a + this.b
  }

}
