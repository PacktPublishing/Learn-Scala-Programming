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

  // assessment
  final case class ReleaseFish[A](fish: Fish, f: Unit => A) extends Action[A]

  final case class Done[F[_]: Functor, A](a: A) extends Free[F, A]
  final case class Join[F[_]: Functor, A](action: F[Free[F, A]]) extends Free[F, A]

  class Free[F[_]: Functor, A] {
    def flatMap[B](f: A => Free[F, B]): Free[F, B] = this match {
      case Done(a) => f(a)
      case Join(a) => Join(implicitly[Functor[F]].map(a)(_.flatMap(f)))
    }
    def map[B](f: A => B): Free[F, B] = flatMap(a => Done(f(a)))
  }

  implicit lazy val actionFunctor: Functor[Action] = new Functor[Action] {
    override def map[A, B](in: Action[A])(f: A => B): Action[B] = in match {
      case BuyBate(name, a) => BuyBate(name, x => f(a(x)))
      case CastLine(bate, a) => CastLine(bate, x => f(a(x)))
      case HookFish(line, a) => HookFish(line, x => f(a(x)))
      // assessment
      case ReleaseFish(fish, a) => ReleaseFish(fish, x => f(a(x)))
    }
  }

  def buyBate(name: String): Free[Action, Bate] = Join(BuyBate(name, bate => Done(bate)))
  def castLine(bate: Bate): Free[Action, Line] = Join(CastLine(bate, line => Done(line)))
  def hookFish(line: Line): Free[Action, Fish] = Join(HookFish(line, fish => Done(fish)))

  // assessment
  def releaseFish(fish: Fish): Free[Action, Unit] = Join(ReleaseFish(fish, _ => Done(())))

  def catchFish(bateName: String): Free[Action, _] = for {
    bate <- buyBate(bateName)
    line <- castLine(bate)
    fish <- hookFish(line)
    _ <- releaseFish(fish)
  } yield ()

  def log[A](a: A): Unit = println(a)

  @tailrec
  def goFishingLogging[A](actions: Free[Action, A], unit: Unit): A = actions match {
    case Join(BuyBate(name, f)) =>
      goFishingLogging(f(Bate(name)), log(s"Buying bate $name"))
    case Join(CastLine(bate, f)) =>
      goFishingLogging(f(Line(bate.name.length)), log(s"Casting line with ${bate.name}"))
    case Join(HookFish(line, f)) =>
      goFishingLogging(f(Fish("CatFish")), log(s"Hooking fish from ${line.length} feet"))
    case Done(fish) => fish
    // assessment
    case Join(ReleaseFish(fish, f)) =>
      goFishingLogging(f(()), log(s"Releasing the fish $fish"))

  }

  println(goFishingLogging(catchFish("Crankbait"), ()))

  @tailrec
  def goFishingAcc[A](actions: Free[Action, A], log: List[AnyVal]): List[AnyVal] = actions match {
    case Join(BuyBate(name, f)) =>
      val bate = Bate(name)
      goFishingAcc(f(bate), bate :: log)
    case Join(CastLine(bate, f)) =>
      val line = Line(bate.name.length)
      goFishingAcc(f(line), line :: log)
    case Join(HookFish(line, f)) =>
      val fish = Fish(s"CatFish from ($line)")
      goFishingAcc(f(fish), fish :: log)
    case Done(_) => log.reverse
    // assessment
    case Join(ReleaseFish(fish, f)) =>
      goFishingAcc(f(()), fish.copy(name = fish.name + " released") :: log)

  }

  lazy val log = goFishingAcc(catchFish("Crankbait"), Nil)
  println(log)

}
