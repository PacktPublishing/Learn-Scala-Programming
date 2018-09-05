package ch03

object Imports {
  val next = Math.nextAfter _
  next(10f, 20f)
  val /\ = Math.hypot(_, _)
  /\(10, 20)
}
