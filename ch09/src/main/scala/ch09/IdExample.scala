package ch09

import scala.language.{higherKinds, implicitConversions}

object IdExample extends App {
  import Monad.Id
  import Boat._

  println(move(go[Id], turn[Id])(boat))

}
