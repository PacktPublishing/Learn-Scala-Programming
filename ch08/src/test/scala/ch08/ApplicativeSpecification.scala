package ch08

import org.scalacheck.Prop._
import org.scalacheck._

import scala.language.higherKinds
import scala.util.Try

object ApplicativeSpecification extends Properties("Applicative") {

  def identityProp[A, F[_]](implicit A: Applicative[F],
                            arbFA: Arbitrary[F[A]]): Prop =
    forAll { as: F[A] =>
      A(as)(A.unit((a: A) => a)) == as
    }

  def homomorphism[A, B, F[_]](implicit A: Applicative[F],
                               arbA: Arbitrary[A],
                               arbB: Arbitrary[B],
                               cogenA: Cogen[A]): Prop = {
    forAll((f: A => B, a: A) => {
      A(A.unit(a))(A.unit(f)) == A.unit(f(a))
    })
  }

  def interchange[A, B, F[_]](implicit A: Applicative[F],
                              arbFA: Arbitrary[F[A]],
                              arbA: Arbitrary[A],
                              arbB: Arbitrary[B],
                              cogenA: Cogen[A]): Prop = {
    forAll((f: A => B, a: A) => {
      val leftSide = A(A.unit(a))(A.unit(f))
      val func = (ff: A => B) => ff(a)
      val rightSide = A(A.unit(f))(A.unit(func))
      leftSide == rightSide
    })
  }

  def composeF[A, B, C]: (B => C) => (A => B) => (A => C) = _.compose

  def composition[A, B, C, F[_]](implicit A: Applicative[F],
                                 arbFA: Arbitrary[F[A]],
                                 arbB: Arbitrary[B],
                                 arbC: Arbitrary[C],
                                 cogenA: Cogen[A],
                                 cogenB: Cogen[B]): Prop = {
    forAll((as: F[A], f: A => B, g: B => C) => {
      val af: F[A => B] = A.unit(f)
      val ag: F[B => C] = A.unit(g)
      val ac: F[(B => C) => (A => B) => (A => C)] = A.unit(composeF)
      val leftSide = A(as)(A(af)(A(ag)(ac)))
      val rightSide = A(A(as)(af))(ag)

      leftSide == rightSide
    })
  }

  def applicative[A, B, C, F[_]](implicit fu: Applicative[F],
                                 arbFA: Arbitrary[F[A]],
                                 arbA: Arbitrary[A],
                                 arbB: Arbitrary[B],
                                 arbC: Arbitrary[C],
                                 cogenA: Cogen[A],
                                 cogenB: Cogen[B]): Prop = {
    identityProp[A, F] && homomorphism[A, B, F] &&
      interchange[A, B, F] && composition[A, B, C, F] &&
      ch08.FunctorSpecification.functor[A, B, C, F]
  }

  import Applicative._

  property("Applicative[Option] and Int => String, String => Long") = {
    applicative[Int, String, Long, Option]
  }

  property("Applicative[Option] and String => Int, Int => Boolean") = {
    applicative[String, Int, Boolean, Option]
  }

  property("Applicative[Try] and Int => String, String => Long") = {
    applicative[Int, String, Long, Try]
  }

  property("Applicative[Try] and String => Int, Int => Boolean") = {
    applicative[String, Int, Boolean, Try]
  }

  type UnitEither[R] = Either[Unit, R]

  property("Applicative[Either] and Int => String, String => Long") = {
    applicative[Int, String, Long, UnitEither]
  }

  property("Applicative[Either] and String => Int, Int => Boolean") = {
    applicative[String, Int, Boolean, UnitEither]
  }

  property("Applicative[List] and Int => String, String => Long") = {
    applicative[Int, String, Long, List]
  }

  property("Applicative[List] and String => Int, Int => Boolean") = {
    applicative[String, Int, Boolean, List]
  }
}
