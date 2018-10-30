package ch02


object ExistentialTypes {

  import ch02.Contravariance._

  def drink[_ <: Water](g: Glass[_]): Unit = {
    g.contents; ()
  }

  import scala.language.existentials

  val glass = Full[T forSome { type T <: Water }](new Water(100))
}
