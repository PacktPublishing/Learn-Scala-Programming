package ch15

import ch15.model._
import com.lightbend.lagom.scaladsl.api.Service._
import com.lightbend.lagom.scaladsl.api._
import play.api.libs.json.{Format, Json}
import ShopService._
import com.lightbend.lagom.scaladsl.api.transport.Method

trait ShopService extends Service {
  def order: ServiceCall[Order, Purchase]

  override def descriptor: Descriptor = {
    named("GroceryShop").withCalls(restCall(Method.POST, "/purchase", order))
  }
}

object ShopService {
  final case class Order(order: ShoppingList)
  final case class Purchase(order: Groceries)
  implicit val purchase: Format[Purchase] = Json.format
  implicit val order: Format[Order] = Json.format
}
