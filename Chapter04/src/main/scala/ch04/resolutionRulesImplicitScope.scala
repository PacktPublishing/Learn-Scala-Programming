package ch04

import scala.language.implicitConversions

trait ParentA { def name: String }
trait ParentB
class ChildA(val name: String) extends ParentA with ParentB

object ParentB {
  implicit def a2Char(a: ParentA): Char = a.name.head

}
object ParentA {
  implicit def a2Int(a: ParentA): Int = a.hashCode()
  implicit val ordering = new Ordering[ChildA] {
    override def compare(a: ChildA, b: ChildA): Int =
      implicitly[Ordering[String]].compare(a.name, b.name)
  }
}
object ChildA {
  implicit def a2String(a: ParentA): String = a.name
}

trait Test {
  def test(a: ChildA) = {
    val _: Int = a // companion object of ParentA
    // val _: String = a // companion object of ChildA
    // val _: Char = a // companion object of ParentB
  }
  def constructor[T: Ordering](in: T*): List[T] = in.toList.sorted // companion object of type constructor
  constructor(new ChildA("A"), new ChildA("B")).sorted // companion object of type parameters
}
