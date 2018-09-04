package ch04

trait ParentA {

}

object ChildA extends ParentA {

}

trait ParentB {

}
class ChildB extends ParentB {

}
object ChildB extends ParentB {

}

trait ParentF[T] {
  def fuse(a: T, b: T): T
}

class StringF extends ParentF[String] {
  override def fuse(a: String, b: String): String = a + b
}

class IntF extends ParentF[Int] {
  override def fuse(a: Int, b: Int): Int = a + b
}
