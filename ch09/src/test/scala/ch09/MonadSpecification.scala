package ch09

import ch09.Monad.Id
import org.scalacheck._
import Prop._
import ch07.Monoid

import scala.language.higherKinds
import scala.util.Try

object MonadSpecification extends Properties("Monad") with MonadArbitrary {

  def associativity[A, B, C, M[_]](implicit M: Monad[M],
                                   arbMA: Arbitrary[M[A]],
                                   arbMB: Arbitrary[M[B]],
                                   arbMC: Arbitrary[M[C]],
                                   arbB: Arbitrary[B],
                                   arbC: Arbitrary[C],
                                   cogenA: Cogen[A],
                                   cogenB: Cogen[B]): Prop = {
    forAll((as: M[A], f: A => M[B], g: B => M[C]) => {
      val leftSide = M.flatMap(M.flatMap(as)(f))(g)
      val rightSide = M.flatMap(as)(a => M.flatMap(f(a))(g))
      leftSide == rightSide
    })
  }

  def id[A, B, M[_]](implicit M: Monad[M],
                     arbFA: Arbitrary[M[A]],
                     arbFB: Arbitrary[M[B]],
                     arbA: Arbitrary[A],
                     cogenA: Cogen[A]): Prop = {
    val leftIdentity = forAll { as: M[A] =>
      M.flatMap(as)(M.unit(_)) == as
    }
    val rightIdentity = forAll { (a: A, f: A => M[B]) =>
      M.flatMap(M.unit(a))(f) == f(a)
    }
    leftIdentity && rightIdentity
  }

  def monad[A, B, C, M[_]](implicit M: Monad[M],
                           arbMA: Arbitrary[M[A]],
                           arbMB: Arbitrary[M[B]],
                           arbMC: Arbitrary[M[C]],
                           arbA: Arbitrary[A],
                           arbB: Arbitrary[B],
                           arbC: Arbitrary[C],
                           cogenA: Cogen[A],
                           cogenB: Cogen[B]): Prop = {
    id[A, B, M] && associativity[A, B, C, M]
  }

    property("Monad[Id] and Int => String, String => Long") = {
      monad[Int, String, Long, Id]
    }

    property("Monad[Id] and String => Int, Int => Boolean") = {
      monad[String, Int, Boolean, Id]
    }

  property("Monad[Option] and Int => String, String => Long") = {
    monad[Int, String, Long, Option]
  }

  property("Monad[Option] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, Option]
  }

  type UnitEither[R] = Either[Unit, R]

  property("Monad[UnitEither[Int]] and Int => String, String => Long") = {
    monad[Int, String, Long, UnitEither]
  }

  property("Monad[UnitEither[String]] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, UnitEither]
  }

  property("Monad[Try] and Int => String, String => Long") = {
    monad[Int, String, Long, Try]
  }

  property("Monad[Try] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, Try]
  }

  property("Monad[List] and Int => String, String => Long") = {
    monad[Int, String, Long, List]
  }

  property("Monad[List] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, List]
  }

  type LongState[A] = State[Long, A]

  implicit val intGen = Gen.posNum[Int]
  implicit val longGen = Gen.posNum[Long]
  implicit val stringGen = Gen.alphaStr
  implicit val boolGen = Gen.oneOf(true, false)

  property("Monad[LongState] and Int => String, String => Long") = {
    monad[Int, String, Long, LongState]
  }

  property("Monad[IntState] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, LongState]
  }

  type StringState[A] = State[String, A]

  property("Monad[StringState] and Int => String, String => Long") = {
    monad[Int, String, Long, StringState]
  }

  property("Monad[StringState] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, StringState]
  }

  type StringWriter[A] = Writer[String, A]

  property("Monad[StringWriter] and Int => String, String => Long") = {
    monad[Int, String, Long, StringWriter]
  }

  property("Monad[StringWriter] and String => Int, Int => Boolean") = {
    monad[String, Int, Boolean, StringWriter]
  }

}

trait MonadArbitrary {
  implicit  def stateGen[S, A](implicit co1: Cogen[S], genA: Gen[A], genS: Gen[S]): Gen[State[S, A]] = {
    val genTuple: Gen[(A, S)] = for {
      a <- genA
      s <- genS
    } yield (a, s)
    for {
      run <- Gen.function1[S, (A, S)](genTuple)
    } yield State(run)
  }

  implicit def writerGen[W : Monoid, A](implicit co1: Cogen[W], genA: Gen[A], genW: Gen[W]): Gen[Writer[W, A]] = {
    val genTuple: Gen[(A, W)] = for {
      a <- genA
      w <- genW
    } yield (a, w)
    for {
      run <- Gen.function0[(A, W)](genTuple)
    } yield Writer(run)
  }

  implicit def arbitraryState[S, A](implicit co1: Cogen[S], genA: Gen[A], genS: Gen[S]): Arbitrary[State[S, A]] =
    Arbitrary(stateGen)

  implicit def arbitraryWriter[W : Monoid, A](implicit co1: Cogen[W], genA: Gen[A], genW: Gen[W]): Arbitrary[Writer[W, A]] =
    Arbitrary(writerGen)

  implicit def arbitraryList[A : Arbitrary] = Arbitrary.arbContainer[List, A]

}
