package ch14

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import ch14.Commands.{PurchaseArticles, RestockArticles}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration._

class RoutesSpec
    extends WordSpec
    with Matchers
    with ScalaFutures
    with ScalatestRouteTest
    with Routes {

  override lazy val config: Config = Config.load()

  DB.initialize(config.database)

  override lazy val inventory: ActorRef =
    system.actorOf(InventoryActor.props, "inventory")

  "Routes" should {
    "return no articles in the beginning" in {
      val request = HttpRequest(uri = "/inventory")
      implicit val timeout: Duration = 3.seconds
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"state":{}}"""
      }
    }
    "be able to add article (POST /articles/eggs)" in {
      val request = Post("/articles/eggs")
      request ~> routes ~> check {
        status shouldBe StatusCodes.Created
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"name":"eggs","count":0}"""
      }
    }
    "not be able to delete article (delete /articles/no)" in {
      val request = Delete("/articles/no-such-article")
      request ~> Route.seal(routes) ~> check {
        status shouldBe StatusCodes.NotFound
      }
    }
    "not be able to add article twice (POST /articles/eggs)" in {
      val request = Post("/articles/eggs")
      request ~> routes ~> check {
        status shouldBe StatusCodes.Conflict
      }
    }
    "be able to restock articles (POST /restock)" in {
      val restock = RestockArticles(Map("eggs" -> 10, "chocolate" -> 20))
      val entity  = Marshal(restock).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val request = Post("/restock").withEntity(entity)
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"stock":{"eggs":10,"chocolate":20}}"""
      }
    }
    "be able to purchase articles (POST /purchase)" in {
      val restock = PurchaseArticles(Map("eggs" -> 5, "chocolate" -> 10))
      val entity  = Marshal(restock).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val request = Post("/purchase").withEntity(entity)
      request ~> routes ~> check {
        status shouldBe StatusCodes.OK
        contentType shouldBe ContentTypes.`application/json`
        entityAs[String] shouldBe """{"order":{"eggs":5,"chocolate":10}}"""
      }
    }
    "not be able to purchase articles (POST /purchase)" in {
      val restock = PurchaseArticles(Map("eggs" -> 50, "chocolate" -> 10))
      val entity  = Marshal(restock).to[MessageEntity].futureValue // futureValue is from ScalaFutures
      val request = Post("/purchase").withEntity(entity)
      request ~> routes ~> check {
        status shouldBe StatusCodes.Conflict
      }
    }
  }
}
