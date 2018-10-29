package ch02


object Covariance {

  sealed trait Glass[+Contents]

  case class Full[Contents](contents: Contents) extends Glass[Contents]

  case object Empty extends Glass[Nothing]

  class Water(purity: Int)

  def drink(glass: Glass[Water]): Unit = ???

  drink(Full(new Water(100)))
  drink(Empty)
}

