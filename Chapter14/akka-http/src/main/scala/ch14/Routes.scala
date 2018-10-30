package ch14

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.{delete, get, post}
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import ch14.Commands._
import ch14.Events.{
  ArticleCreated,
  ArticleDeleted,
  ArticlesPurchased,
  ArticlesRestocked
}

import scala.concurrent.{ExecutionContext, Future}

trait Routes extends JsonSupport {
  implicit def system: ActorSystem
  def inventory: ActorRef
  def config: Config

  implicit lazy val timeout: Timeout = config.timeout
  implicit lazy val ec: ExecutionContext = system.dispatcher

  lazy val articlesRoutes: Route =
    pathPrefix("articles") {
      concat(
        path(Segment) { name =>
          concat(
            post {
              val changedInventory: Future[Option[ArticleCreated]] =
                (inventory ? CreateArticle(name, 0))
                  .mapTo[Option[ArticleCreated]]
              onSuccess(changedInventory) {
                case None        => complete(StatusCodes.Conflict)
                case Some(event) => complete(StatusCodes.Created, event)
              }
            },
            delete {
              val changedInventory: Future[Option[ArticleDeleted]] =
                (inventory ? DeleteArticle(name)).mapTo[Option[ArticleDeleted]]
              rejectEmptyResponse {
                complete(changedInventory)
              }
            },
            get {
              complete((inventory ? GetArticle(name)).mapTo[Inventory])
            }
          )
        }
      )
    }

  lazy val inventoryRoutes: Route =
    path("inventory") {
      get {
        complete((inventory ? GetInventory).mapTo[Inventory])
      }
    } ~
      path("purchase") {
        post {
          entity(as[PurchaseArticles]) { order =>
            val response: Future[Option[ArticlesPurchased]] =
              (inventory ? order).mapTo[Option[ArticlesPurchased]]
            onSuccess(response) {
              case None        => complete(StatusCodes.Conflict)
              case Some(event) => complete(event)
            }
          }
        }
      } ~
      path("restock") {
        post {
          entity(as[RestockArticles]) { stock =>
            val response: Future[Option[ArticlesRestocked]] =
              (inventory ? stock).mapTo[Option[ArticlesRestocked]]
            complete(response)
          }
        }
      }


  lazy val routes: Route = articlesRoutes ~ inventoryRoutes

}
