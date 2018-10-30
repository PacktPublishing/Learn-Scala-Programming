package ch03

object Closures {
  def outerA = {
    val free = 5

    def innerA = {
      val free = 20

      def closure(in: Int) = free + in

      closure(10)
    }

    innerA + free
  }

  outerA

  def forwardReference(in: Int) = {
    // def closure(input: Int) = input + free + in // compile error

    val free = 30
  }
}
