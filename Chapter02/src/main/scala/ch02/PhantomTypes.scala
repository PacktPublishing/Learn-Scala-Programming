package ch02


object PhantomTypes {

  sealed trait LockState

  sealed trait Open extends LockState

  sealed trait Closed extends LockState

  sealed trait Broken extends LockState

  case class Lock[State <: LockState]() {
    def open[_ >: State <: Closed]: Lock[Open] = Lock()

    def close[_ >: State <: Open]: Lock[Closed] = Lock()

    def break: Lock[Broken] = Lock()
  }

  val openLock = Lock[Open]

  val closedLock = openLock.close
  val broken = closedLock.break

  // closedLock.close() // compile error
  // openLock.open() // compile error
  // broken.open() // compile error
}
