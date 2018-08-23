package ch02


object TypeMembers {

  trait HolderA {
    type A

    def a: A
  }

  class A extends HolderA {
    override type A = Int

    override def a = 10
  }

  abstract class HolderBC {
    type B
    type C <: B

    def b: B

    def c: C
  }

  // fails to compile
  /*
  class BC extends HolderBC {
    override def b = "String"
    override def c = true
  }
   */

  trait HolderDEF {
    type D >: Null <: AnyRef
    type E <: AnyVal
    type F = this.type

    def d: D

    def e: E

    def f: F
  }

  class DEF extends HolderDEF {
    override type D = String
    override type E = Boolean

    // incompatible type
    // override type E = String
    // override def e = true

    override def d = ""

    override def e = true

    // incompatible type
    // override def f: DEF = this
    override def f: this.type = this
  }

  abstract class HolderGH[G, H] {
    type I <: G
    type J >: H

    def apply(j: J): I
  }

  class GH extends HolderGH[String, Null] {
    override type I = Nothing
    override type J = String

    override def apply(j: J): I = throw new Exception
  }

  trait Rule[In] {
    type Out

    def method(in: In): Out
  }

}

