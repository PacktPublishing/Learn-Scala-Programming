package ch15

import ch15.model._
import com.lightbend.lagom.scaladsl.api._
import Service._

trait BoyService extends Service {
  def shop: ServiceCall[ShoppingList, Groceries]

  override def descriptor: Descriptor = {
    named("BoyService").withCalls(namedCall("go-shopping", shop))
  }
}
