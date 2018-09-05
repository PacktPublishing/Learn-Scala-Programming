package ch04

object ViewBounds {

  case class CanEqual(hash: Int)

  def equal[CA, CB](a: CA, b: CB)(implicit ca: CA => CanEqual, cb: CB => CanEqual): Boolean =
    ca(a).hash == ca(a).hash

  def equalsWithBounds[CA <% CanEqual, CB <% CanEqual](a: CA, b: CB): Boolean = {
    val hashA = implicitly[CA => CanEqual].apply(a).hash
    val hashB = implicitly[CB => CanEqual].apply(b).hash
    hashA == hashB
  }

  def equalsWithPassing[CA <% CanEqual, CB <% CanEqual](a: CA, b: CB): Boolean =
    equal(a, b)

  import scala.language.implicitConversions

  implicit def str2CanEqual(s: String): CanEqual = CanEqual(s.hashCode)

  implicit class IntCanEqual(i: Int) extends CanEqual(i)

  def test: Boolean = equalsWithPassing("a", "b")

  def test1: Boolean = equalsWithBounds("a", 20)

  def test2: Boolean = equalsWithBounds("a", 97)
}

/*
ViewBounds.test
ViewBounds.test1
ViewBounds.test2*/
