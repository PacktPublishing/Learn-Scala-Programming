package ch08

import org.scalacheck._
import org.scalacheck.Prop._

import scala.language.higherKinds

object FunctorSpecification extends Properties("Functor") {

  def id[A, F[_]](implicit fu: Functor[F], arbFA: Arbitrary[F[A]]): Prop =
    forAll { as: F[A] => fu.map(as)(identity) == as }

  def associativity[A, B, C, F[_]](implicit fu: Functor[F],
                                   arbFA: Arbitrary[F[A]],
                                   arbB: Arbitrary[B],
                                   arbC: Arbitrary[C],
                                   cogenA: Cogen[A],
                                   cogenB: Cogen[B]): Prop = {
    val f = Arbitrary.arbFunction1[A, B]
    val g = Arbitrary.arbFunction1[B, C]
    forAll((as: F[A], f: A => B, g: B => C) => {
      fu.map(fu.map(as)(f))(g) == fu.map(as)(f andThen g)
    })
  }

  def functor[A, B, C, F[_]](implicit fu: Functor[F], arbFA: Arbitrary[F[A]],
                             arbB: Arbitrary[B],
                             arbC: Arbitrary[C],
                             cogenA: Cogen[A],
                             cogenB: Cogen[B]): Prop =
    id[A, F] && associativity[A, B, C, F]

  property("Functor[Option] and Int => String, String => Long") = {
    import Functor.optionFunctor
    functor[Int, String, Long, Option]
  }

  property("Functor[Option] and String => Int, Int => Boolean") = {
    import Functor.optionFunctor
    functor[String, Int, Boolean, Option]
  }

  property("Functor[Either] and Int => String, String => Long") = {
    import Functor.eitherFunctor
    functor[Int, String, Long, Option]
  }

  property("Functor[Either] and String => Int, Int => Boolean") = {
    import Functor.eitherFunctor
    functor[String, Int, Boolean, Option]
  }

  property("Functor[Try] and Int => String, String => Long") = {
    import Functor.tryFunctor
    functor[Int, String, Long, Option]
  }

  property("Functor[Try] and String => Int, Int => Boolean") = {
    import Functor.tryFunctor
    functor[String, Int, Boolean, Option]
  }


}
