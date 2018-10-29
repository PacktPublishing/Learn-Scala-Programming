package ch02


object TypeLambdas {

  import scala.language.higherKinds
  import scala.language.reflectiveCalls

  sealed trait Contents

  case class Water(purity: Int) extends Contents

  case class Whiskey(label: String) extends Contents

  sealed trait Container[C] {
    def contents: C
  }

  case class Glass[C](contents: C) extends Container[C]

  case class Jar[C](contents: C) extends Container[C]

  sealed trait Filler[C <: Contents, CC <: Container[C]] {
    def fill(c: C): CC
  }

  type WaterFiller[CC <: Container[Water]] = Filler[Water, CC]

  def fillWithWater[CC <: Container[Water]](container: CC)(filler: WaterFiller[CC]) = ???

}

