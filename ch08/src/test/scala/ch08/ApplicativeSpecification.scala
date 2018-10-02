package ch08

import org.scalacheck.Prop._
import org.scalacheck._

import scala.language.higherKinds

object ApplicativeSpecification extends Properties("Applicative") {

  def identityProp[A, F[_]](implicit A: Applicative[F], arbFA: Arbitrary[F[A]]): Prop =
    forAll { as: F[A] =>
      A.apply(as)(A.unit((a: A) => a)) == as
    }

  def homomorphism[A, B, F[_]](implicit A: Applicative[F],
                               arbA: Arbitrary[A],
                               arbB: Arbitrary[B],
                               cogenA: Cogen[A]): Prop = {
    forAll((f: A => B, a: A) => {
      A.apply(A.unit(a))(A.unit(f)) == A.unit(f(a))
    })
  }

  def interchange[A, B, F[_]](implicit A: Applicative[F],
                                 arbFA: Arbitrary[F[A]],
                                 arbA: Arbitrary[A],
                                 arbB: Arbitrary[B],
                                 cogenA: Cogen[A]
                                 ): Prop = {
    forAll((as: F[A], f: A => B, a: A) => {
      val leftSide = A.apply(A.unit(a))(A.unit(f))
      val func = (ff: A => B) => ff(a)
      val rightSide =  A.apply(A.unit(f))(A.unit(func))
      leftSide == rightSide
    })
  }

  def compose[A, B, C]: (B=>C) => (A=>B) => (A=>C) = _.compose

  def composition[A, B, C, F[_]](implicit A: Applicative[F],
                                 arbFA: Arbitrary[F[A]],
                                 arbB: Arbitrary[B],
                                 arbC: Arbitrary[C],
                                 cogenA: Cogen[A],
                                 cogenB: Cogen[B]): Prop = {
    forAll((as: F[A], f: A => B, g: B => C) => {
      val af: F[A => B] = A.unit(f)
      val ag: F[B => C] = A.unit(g)
      val ac: F[(B=>C) => (A=>B) => (A=>C)] = A.unit(compose)
      val leftSide = A.apply(as)(A.apply(af)(A.apply(ag)(ac)))
      val rightSide = A.apply(A.apply(as)(af))(ag)

      leftSide == rightSide
    })
  }



  def applicativeOnly[A, B, C, F[_]](implicit fu: Applicative[F], arbFA: Arbitrary[F[A]],
                                     arbA: Arbitrary[A],
                                     arbB: Arbitrary[B],
                                     arbC: Arbitrary[C],
                                     cogenA: Cogen[A],
                                     cogenB: Cogen[B]): Prop =
    identityProp[A, F] && homomorphism[A, B, F] && interchange[A, B, F] && composition[A, B, C, F]

  def applicative[A, B, C, F[_]](implicit fu: Applicative[F], arbFA: Arbitrary[F[A]],
                                 arbA: Arbitrary[A],
                                 arbB: Arbitrary[B],
                                 arbC: Arbitrary[C],
                                 cogenA: Cogen[A],
                                 cogenB: Cogen[B]): Prop =
    applicativeOnly[A, B, C, F] && FunctorSpecification.functor[A, B, C, F]

  /*

    property("Functor[Option] and Int => String, String => Long") = {
      import Functor.optionFunctor
      functor[Int, String, Long, Option]
    }

    property("Functor[Option] and String => Int, Int => Boolean") = {
      import Functor.optionFunctor
      functor[String, Int, Boolean, Option]
    }

    property("Functor[Either] and Int => String, String => Long") = {
      functor[Int, String, Long, Option]
    }

    property("Functor[Either] and String => Int, Int => Boolean") = {
      functor[String, Int, Boolean, Option]
    }

    property("Functor[Try] and Int => String, String => Long") = {
      functor[Int, String, Long, Option]
    }

    property("Functor[Try] and String => Int, Int => Boolean") = {
      functor[String, Int, Boolean, Option]
    }
  */


}
