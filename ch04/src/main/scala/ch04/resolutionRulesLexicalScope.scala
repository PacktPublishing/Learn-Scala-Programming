package ch04

package object resolution {
  implicit val a: TS = new TS("val in package object") // (1)
}

package resolution {
  class TS(override val toString: String)
  class Parent {
    // implicit val c: TS = new TS("val in parent class") // (2)
  }
  trait Mixin {
    // implicit val d: TS = new TS("val in mixin") // (3)
  }
  // import Outer._ // (4)
  class Outer {
    // implicit val e: TS = new TS("val in outer class") // (5)
    // import Inner._ // (6)

    class Inner(/*implicit (7) */ val arg: TS = implicitly[TS]) extends Parent with Mixin {
      // implicit val f: TS = new TS("val in inner class") (8)
      private val resolve = implicitly[TS]
    }
    object Inner {
      implicit val g: TS = new TS("val in companion object")
    }
  }
  object Outer {
    implicit val h: TS = new TS("val in parent companion object")
  }
}
