
import org.scalacheck._
import org.scalacheck.Prop._

object Properties {

  object Introduction {
    val stringLengthProp: Prop = forAll { (_: String).length >= 0 }

    stringLengthProp.check
  }

  object Commutativity {
    forAll((a: Int, b: Int) => a + b == b + a).check

    forAll((a: Int, b: Int) => a * b == b * a).check

    forAll((a: String, b: String) => a + b == b + a).check

    forAll((a: String) => a + "" == "" + a).check
  }

  object Associativity {
    forAll((a: Int, b: Int, c: Int) => (a + b) + c == a + (b + c)).check

    forAll((a: Int, b: Int, c: Int) => (a * b) * c == a * (b * c)).check

    forAll((a: String, b: String, c: String) => (a + b) + c == a + (b + c)).check
  }

  object Identity {
    forAll((a: Int) => a + 0 == a && 0 + a == a).check

    forAll((a: Int) => a * 1 == a && 1 * a == a).check

    forAll((a: String) => a + "" == a && "" + a == a).check
  }

  object Invariants {
    forAll((a: String) => a.toUpperCase().length == a.length).check
    forAll((a: String) => a.sorted.length == a.length).check
    forAll(Gen.asciiStr)((a: String) => a.toUpperCase().length == a.length).check
  }

  object Idempotency {
    forAll((a: String) => a.toUpperCase().toUpperCase() == a.toUpperCase()).check
    forAll((a: String) => a.sorted.sorted == a.sorted).check
    forAll((a: Int) =>  a * 0 * 0 == a * 0) .check
  }

  object Induction {
      def factorial(n: Long): Long = if (n < 2) n else n * factorial(n-1)
      forAll((a: Byte) => a > 2 ==> (factorial(a) == a * factorial(a - 1))).check
  }

  object Symmetry {
    forAll((a: String) => a.reverse.reverse == a).check
    forAll((a: Int, b: Int) => a + b - b == a).check
  }

  object TestOracle {
    forAll { a: String =>
      val chars = a.toCharArray
      java.util.Arrays.sort(chars)
      val b = String.valueOf(chars)
      a.sorted == b
    }.check
  }

  object Checking {
    private val prop = forAll { a: String => a.reverse.reverse == a }
    private val timed = within(10000)(prop)
    Test.check(timed) {
      _.withMinSuccessfulTests(100000).withWorkers(4).withMaxDiscardRatio(3)
    }

    forAll { a: String =>
      classify(a.isEmpty, "empty string", "non-empty string") {
        val result = a.sorted.length == a.length
        result
      }
    }.check()


    val prop2 = "Division by zero" |: protect(forAll((a: Int) => a / a ?= 1))
    prop2.check()
  }

  object Combining {
    forAll { (a: Int, b: Int, c: Int, d: String) =>
      val multiplicationLaws = all(
        "Commutativity" |: (a * b ?= b * a),
        "Associativity" |:  ((a * b) * c ?= a * (b * c)),
        "Identity" |: all(a * 1 ?= a, 1 * a ?= a)
      ) :| "Multiplication laws"
      val stringProps = atLeastOne(d.isEmpty, d.nonEmpty)
      all(multiplicationLaws, stringProps)
    }.check()
  }

}
