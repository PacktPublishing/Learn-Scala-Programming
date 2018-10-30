package ch14

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import ch14.Commands._
import ch14.Events._
import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import stamina.StaminaAkkaSerializer
import stamina.json._

trait JsonSupport extends SprayJsonSupport {

  import DefaultJsonProtocol._

  type RJF[T] = RootJsonFormat[T]

  implicit val createArticleJF: RJF[CreateArticle] = jsonFormat2(CreateArticle)
  implicit val deleteArticleJF: RJF[DeleteArticle] = jsonFormat1(DeleteArticle)
  implicit val purchaseJF: RJF[PurchaseArticles] = jsonFormat1(PurchaseArticles)
  implicit val restockJF: RJF[RestockArticles] = jsonFormat1(RestockArticles)

  implicit val createdJF: RJF[ArticleCreated] = jsonFormat2(ArticleCreated)
  implicit val deletedJF: RJF[ArticleDeleted] = jsonFormat1(ArticleDeleted)
  implicit val pJF: RJF[ArticlesPurchased] = jsonFormat1(ArticlesPurchased)
  implicit val reJF: RJF[ArticlesRestocked] = jsonFormat1(ArticlesRestocked)

  implicit val invJF: RJF[Inventory] = jsonFormat1(Inventory)

}

object PersistenceSupport extends JsonSupport {
  val v1createdP = persister[ArticleCreated]("article-created")
  val v1deletedP = persister[ArticleDeleted]("article-deleted")
  val v1purchasedP = persister[ArticlesPurchased]("articles-purchased")
  val v1restockedP = persister[ArticlesRestocked]("articles-restocked")
  val v1inventoryP = persister[Inventory]("inventory")
}

import PersistenceSupport._

class EventSerializer
    extends StaminaAkkaSerializer(v1createdP,
                                  v1deletedP,
                                  v1purchasedP,
                                  v1restockedP,
                                  v1inventoryP)
