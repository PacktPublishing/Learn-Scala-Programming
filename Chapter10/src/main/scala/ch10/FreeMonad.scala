package ch10

import ch08.Functor

import scala.annotation.tailrec
import scala.language.higherKinds

object FreeMonad extends App {

  case class Bait(name: String) extends AnyVal
  case class Line(length: Int) extends AnyVal
  case class Fish(name: String) extends AnyVal

  sealed trait Action[A]
  final case class BuyBait[A](name: String, f: Bait => A) extends Action[A]
  final case class CastLine[A](bait: Bait, f: Line => A) extends Action[A]
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
      case BuyBait(name, a) => BuyBait(name, x => f(a(x)))
      case CastLine(bait, a) => CastLine(bait, x => f(a(x)))
      case HookFish(line, a) => HookFish(line, x => f(a(x)))
      // assessment
      case ReleaseFish(fish, a) => ReleaseFish(fish, x => f(a(x)))
    }
  }

  def buyBait(name: String): Free[Action, Bait] = Join(BuyBait(name, bait => Done(bait)))
  def castLine(bait: Bait): Free[Action, Line] = Join(CastLine(bait, line => Done(line)))
  def hookFish(line: Line): Free[Action, Fish] = Join(HookFish(line, fish => Done(fish)))

  // assessment
  def releaseFish(fish: Fish): Free[Action, Unit] = Join(ReleaseFish(fish, _ => Done(())))

  def catchFish(baitName: String): Free[Action, _] = for {
    bait <- buyBait(baitName)
    line <- castLine(bait)
    fish <- hookFish(line)
    _ <- releaseFish(fish)
  } yield ()

  def log[A](a: A): Unit = println(a)

  @tailrec
  def goFishingLogging[A](actions: Free[Action, A], unit: Unit): A = actions match {
    case Join(BuyBait(name, f)) =>
      goFishingLogging(f(Bait(name)), log(s"Buying bait $name"))
    case Join(CastLine(bait, f)) =>
      goFishingLogging(f(Line(bait.name.length)), log(s"Casting line with ${bait.name}"))
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
    case Join(BuyBait(name, f)) =>
      val bait = Bait(name)
      goFishingAcc(f(bait), bait :: log)
    case Join(CastLine(bait, f)) =>
      val line = Line(bait.name.length)
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
