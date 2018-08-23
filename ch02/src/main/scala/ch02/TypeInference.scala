package ch02


object TypeInference {

  case class C()

  class D()

  case class E()

  trait Foo {
    def foo: Int
  }

  case class F() extends Foo {
    def foo: Int = 0
  }

  case class G() extends Foo {
    def foo: Int = 0
  }

  def intOrBool(i: Int, s: Boolean)(b: Boolean): AnyVal = if (b) i else s

  def intOrString(i: Int, s: String)(b: Boolean): Any = if (b) i else s

  def stringOrC(c: C, s: String)(b: Boolean): java.io.Serializable =
    if (b) c else s

  def cOrD(c: C, d: D)(b: Boolean): AnyRef = if (b) c else d

  def cOrE(c: C, e: E)(b: Boolean): Product with Serializable = if (b) c else e

  def fOrG(f: F, g: G)(b: Boolean): Product with Serializable with Foo =
    if (b) f else g
}
