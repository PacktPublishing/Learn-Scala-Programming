package ch03


object Functions {
  def method(name: String) = {
    def function(in1: Int, in2: String, in3: Boolean): String = name + in2
    function _
  }
  val function = method("name")

  def fourParams(one: String, two: Int, three: Boolean, four: Long) = ()
  val applyTwo = fourParams("one", _: Int, true, _: Long)
}
