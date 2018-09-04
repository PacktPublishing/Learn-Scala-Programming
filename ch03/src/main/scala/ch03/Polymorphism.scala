package ch03

object Polymorphism {
  sealed trait Glass[+Contents]

  case class Full[Contents](contents: Contents) extends Glass[Contents]

  case object Empty extends Glass[Nothing]

  case class Water(purity: Int)

  def drink(glass: Glass[Water]): Unit = ???

  def drinkAndRefill[B](glass: Glass[B]): Glass[B] = ???

  def drinkAndRefillWater[B >: Water, C >: B](glass: Glass[B]): Glass[C] = glass

  drinkAndRefill[Nothing](Empty)

  def drinkFun[B] = (glass: Glass[B]) => glass
}
