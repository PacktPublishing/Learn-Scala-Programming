package ch02

object RecursiveTypes {

  sealed trait Secret[E]

  sealed trait Lock[E <: Lock[E]] { self: E  =>
    def open(key: Secret[E]): E = self
  }

  // case class IntLock() extends Lock[Int] // compile error

  case class PadLock() extends Lock[PadLock]
  // case class CombinationLock() extends Lock[PadLock] // compile error

  case class CombinationLock() extends Lock[CombinationLock]


  val unlocked: PadLock = PadLock().open(new Secret[PadLock]{})
  CombinationLock().open(new Secret[CombinationLock]{})
}
