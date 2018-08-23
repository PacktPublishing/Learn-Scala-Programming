package ch02

object GeneralisedPhantomTypes {

  sealed trait LockState
  sealed trait Open extends LockState
  sealed trait Closed extends LockState
  sealed trait Broken extends LockState

  case class Lock[State <: LockState]() {
    def break: Lock[Broken] = Lock()
    def open(implicit ev: State =:= Closed): Lock[Open] = Lock()
    def close(implicit ev: State =:= Open): Lock[Closed] = Lock()
  }

  val openLock = Lock[Open]

  val closedLock = openLock.close
  val lock = closedLock.open
  val broken = closedLock.break

  // closedLock.close // compile error
  // openLock.open // compile error
  // broken.open // compile error
}
