package ch01

import scala.collection.StringView
import scala.collection.SortedSet

object Collections213 extends App {


  def transform[C <: Iterable[Char]](i: C): Iterable[Char] = i map { c =>
    print(s"-$c-")
    c.toUpper
  } take {
    println("\ntake")
    6
  }

  val str = "Scala 2.13"
  val view: StringView = StringView(str)

  val transformed = transform(view)
  val strict = transform(str.toList)

  print("Lazy view constructed: ")

  transformed.foreach(print) // forcing

  print("\nLazy view forced: ")

  println(transformed.to(List)) // also forcing

  println(s"Strict: $strict")

  val set = SortedSet(1,2,3)
  val ordered = set.map(math.abs)
  val unordered = set.to(Set).map(math.abs)


  ordered.forall(set)

  val vector = IndexedSeq.fill(2)("A")
}
