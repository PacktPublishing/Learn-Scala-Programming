package ch02


object Invariance {

  sealed trait Glass[Contents]

  case class Full[Contents](contents: Contents) extends Glass[Contents]

  case object Empty extends Glass[Nothing]

  case class Water(purity: Int)

  def drink(glass: Glass[Water]): Unit = ???

  drink(Full(Water(100)))
  // drink(Empty)

  def drinkAndRefill[B <: Water](glass: Glass[B]): Unit = ???

  drinkAndRefill(Empty)
}
