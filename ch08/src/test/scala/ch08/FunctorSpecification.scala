package ch08

import org.scalacheck._
import org.scalacheck.Prop._

import scala.language.higherKinds
import scala.util.Try

object FunctorSpecification extends Properties("Functor") {

  def id[A, F[_]](implicit F: Functor[F], arbFA: Arbitrary[F[A]]): Prop =
    forAll { as: F[A] => F.map(as)(identity) == as }

  def associativity[A, B, C, F[_]](implicit F: Functor[F],
                                   arbFA: Arbitrary[F[A]],
                                   arbB: Arbitrary[B],
                                   arbC: Arbitrary[C],
                                   cogenA: Cogen[A],
                                   cogenB: Cogen[B]): Prop = {
    forAll((as: F[A], f: A => B, g: B => C) => {
      F.map(F.map(as)(f))(g) == F.map(as)(f andThen g)
    })
  }

  def functor[A, B, C, F[_]](implicit F: Functor[F], arbFA: Arbitrary[F[A]],
                             arbB: Arbitrary[B],
                             arbC: Arbitrary[C],
                             cogenA: Cogen[A],
                             cogenB: Cogen[B]): Prop =
    id[A, F] && associativity[A, B, C, F]

  import Functor._

  property("Functor[Option] and Int => String, String => Long") = {
    functor[Int, String, Long, Option]
  }

  property("Functor[Option] and String => Int, Int => Boolean") = {
    functor[String, Int, Boolean, Option]
  }

  type UnitEither[R] = Either[Unit, R]

  property("Functor[Either] and Int => String, String => Long") = {
    functor[Int, String, Long, UnitEither]
  }

  property("Functor[Either] and String => Int, Int => Boolean") = {
    functor[String, Int, Boolean, UnitEither]
  }

  property("Functor[Try] and Int => String, String => Long") = {
    functor[Int, String, Long, Try]
  }

  property("Functor[Try] and String => Int, Int => Boolean") = {
    functor[String, Int, Boolean, Try]
  }


}
