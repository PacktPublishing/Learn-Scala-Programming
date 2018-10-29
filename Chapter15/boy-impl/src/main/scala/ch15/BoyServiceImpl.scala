package ch15

import ch15.ShopService._
import ch15.model._
import com.lightbend.lagom.scaladsl.api._
import play.api.Logger

import scala.concurrent.{ExecutionContext, Future}

class BoyServiceImpl(shopService: ShopService)(implicit ec: ExecutionContext)
    extends BoyService {
  private val logger = Logger("Boy")
  override def shop: ServiceCall[ShoppingList, Groceries] =
    ServiceCall(callExtApi)

  private val callExtApi: ShoppingList => Future[Groceries] = list =>
    shopService.order.invoke(Order(list)).map(_.order).recover {
      case th: Throwable =>
        logger.error(th.getMessage, th)
        Groceries(0, 0, 0, 0)
  }
}
