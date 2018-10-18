package ch10

import ch08.Functor

import scala.annotation.tailrec
import scala.language.higherKinds

object FreeMonad extends App {

  case class Bate(name: String) extends AnyVal
  case class Line(length: Int) extends AnyVal
  case class Fish(name: String) extends AnyVal

  sealed trait Action[A]
  final case class BuyBate[A](name: String, f: Bate => A) extends Action[A]
  final case class CastLine[A](bate: Bate, f: Line => A) extends Action[A]
  final case class HookFish[A](line: Line, f: Fish => A) extends Action[A]

  // Pure builds a Free instance from an A value (it reifies the pure function)
  // Suspend builds a new Free by applying F to a previous Free (it reifies the flatMap function)

  final case class Done[F[_]: Functor, A](a: A) extends Free[F, A]
  final case class Suspend[F[_]: Functor, A](action: F[Free[F, A]]) extends Free[F, A]

  class Free[F[_]: Functor, A] {
    def flatMap[B](f: A => Free[F, B]): Free[F, B] = this match {
      case Done(a) => f(a)
      case Suspend(a) => Suspend(implicitly[Functor[F]].map(a)(_.flatMap(f)))
    }
    def map[B](f: A => B): Free[F, B] = flatMap(a => Done(f(a)))
  }

  implicit val actionFunctor: Functor[Action] = new Functor[Action] {
    override def map[A, B](in: Action[A])(f: A => B): Action[B] = in match {
      case BuyBate(name, a) => BuyBate(name, x => f(a(x)))
      case CastLine(bate, a) => CastLine(bate, x => f(a(x)))
      case HookFish(line, a) => HookFish(line, x => f(a(x)))
    }
  }

  def buyBate(name: String): Free[Action, Bate] = Suspend(BuyBate(name, bate => Done(bate)))
  def castLine(bate: Bate): Free[Action, Line] = Suspend(CastLine(bate, line => Done(line)))
  def hookFish(line: Line): Free[Action, Fish] = Suspend(HookFish(line, fish => Done(fish)))

  def catchFish(bateName: String): Free[Action, Fish] = for {
    bate <- buyBate(bateName)
    line <- castLine(bate)
    fish <- hookFish(line)
  } yield fish

  def log[A](a: A): Unit = println(a)

  @tailrec
  def goFishingLogging(actions: Free[Action, Unit], unit: Unit): Unit = actions match {
    case Suspend(BuyBate(name, f)) => goFishingLogging(f(Bate(name)), log(s"Buying bate $name"))
    case Suspend(CastLine(bate, f)) => goFishingLogging(f(Line(bate.name.length)), log(s"Casting line with ${bate.name}"))
    case Suspend(HookFish(line, f)) => goFishingLogging(f(Fish("CatFish")), log(s"Hooking fish from ${line.length} feet"))
    case Done(_) => ()
  }

  goFishingLogging(catchFish("Crankbait").map(_ => ()), ())

  @tailrec
  def goFishingAcc[A](actions: Free[Action, A], log: List[AnyVal]): List[AnyVal] = actions match {
    case Suspend(BuyBate(name, f)) =>
      val bate = Bate(name)
      goFishingAcc(f(bate), bate :: log)
    case Suspend(CastLine(bate, f)) =>
      val line = Line(bate.name.length)
      goFishingAcc(f(line), line :: log)
    case Suspend(HookFish(line, f)) =>
      val fish = Fish(s"CatFish from ($line)")
      goFishingAcc(f(fish), fish :: log)
    case Done(a) => log
  }

  val log = goFishingAcc(catchFish("Crankbait"), Nil)
  println(log)
}
