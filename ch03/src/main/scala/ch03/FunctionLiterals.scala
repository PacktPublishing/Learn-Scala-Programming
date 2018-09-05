package ch03


object FunctionLiterals {
  val hash: (Int, Boolean, String, Long) => Int = (a, b, c, d) => {
    val ab = 31 * a.hashCode() + b.hashCode()
    val abc = 31 * ab + c.hashCode
    31 * abc + d.hashCode()
  }
  val hashInferred = (a: Int, b: Boolean, c: String, d: Long) => {
    val ab = 31 * a.hashCode() + b.hashCode()
    val abc = 31 * ab + c.hashCode
    31 * abc + d.hashCode()
  }

  def printHash(hasher: String => Int)(s: String): Unit = println(hasher(s))

  val hasher1: String => Int = s => s.hashCode
  val hasher2 = (s: String) => s.hashCode
  printHash(hasher1)("Full")
  printHash(hasher2)("Inferred result type")

  printHash((s: String) => s.hashCode)("inline")
  printHash((s) => s.hashCode)("inline + inference")
  printHash(s => s.hashCode)("single argument parentheses")
  printHash(_.hashCode)("placeholder syntax")

  val hashPlaceholder = (_: Int) * 31^4 + (_: Int) * 31^3 + (_: Int) * 31^2 + (_: Int) * 31
}
