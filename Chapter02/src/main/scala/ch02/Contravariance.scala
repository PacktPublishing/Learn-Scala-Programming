package ch02


object Contravariance {

  class Drinker[-T] {
    def drink(contents: T): Unit = ???
  }

  sealed trait Glass[Contents] {
    def contents: Contents

    def knockBack(drinker: Drinker[Contents]): Unit = drinker.drink(contents)
  }

  case class Full[Contents](contents: Contents) extends Glass[Contents]

  class Water(purity: Int)

  class PureWater(purity: Int) extends Water(purity) {
    def shine(): Unit = ???
  }

  val glass = Full(new PureWater(100))
  glass.knockBack(new Drinker[PureWater])

  glass.knockBack(new Drinker[Water])

}

