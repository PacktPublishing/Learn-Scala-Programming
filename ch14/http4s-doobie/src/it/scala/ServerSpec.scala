import ch14.{Config, Server}
import cats.effect.IO
import cats.implicits._
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.client.blaze.Http1Client
import org.http4s.{Method, Request, Status, Uri}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}
import org.http4s.server.{Server => Http4sServer}

class ServerSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  private lazy val client = Http1Client[IO]().unsafeRunSync()

  private lazy val configIO = Config.load("test.conf")
  private lazy val config = configIO.unsafeRunSync()

  private lazy val rootUrl = s"http://${config.server.host}:${config.server.port}"

  private val server: Option[Http4sServer[IO]] = (for {
    builder <- Server.createServer(configIO)
  } yield builder.start.unsafeRunSync()).compile.last.unsafeRunSync()


  override def afterAll(): Unit = {
    client.shutdown.unsafeRunSync()
    server.foreach(_.shutdown.unsafeRunSync())
  }

  "The server" should {
    "get an empty inventory" in {
      val json = client.expect[Json](s"$rootUrl/inventory").unsafeRunSync()
      json shouldBe json"""{}"""
    }
    "create articles" in {
      val eggs = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/articles/eggs"))
      client.status(eggs).unsafeRunSync() shouldBe Status.NoContent
      val chocolate = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/articles/chocolate"))
      client.status(chocolate).unsafeRunSync() shouldBe Status.NoContent
      val json = client.expect[Json](s"$rootUrl/inventory").unsafeRunSync()
      json shouldBe json"""{"eggs" : 0,"chocolate" : 0}"""
    }
    "update inventory" in {
      val restock = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/restock")).withBody(json"""{ "inventory" : { "eggs": 10, "chocolate": 20 }}""")
      client.expect[Json](restock).unsafeRunSync() shouldBe json"""{ "eggs" : 10, "chocolate" : 20 }"""
      client.expect[Json](restock).unsafeRunSync() shouldBe json"""{ "eggs" : 20, "chocolate" : 40 }"""
    }
    "deliver purchase if there is enough inventory" in {
      val purchase = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/purchase")).withBody(json"""{ "order" : { "eggs": 5, "chocolate": 5 }}""")
      client.expect[Json](purchase).unsafeRunSync() shouldBe json"""{ "eggs" : 5, "chocolate" : 5 }"""
    }
    "not deliver purchase if there is not enough inventory" in {
      val purchase = Request[IO](method = Method.POST, uri = Uri.unsafeFromString(s"$rootUrl/purchase")).withBody(json"""{ "order" : { "eggs": 5, "chocolate": 45 }}""")
      client.expect[Json](purchase).unsafeRunSync() shouldBe json"""{ "eggs" : 0, "chocolate" : 0 }"""
    }
  }

}
