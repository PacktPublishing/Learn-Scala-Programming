package ch14

import cats.effect.IO
import ch14.Model.{Inventory, Purchase, Restock}
import org.http4s.{HttpService, MediaType, Response}
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s.headers.`Content-Type`
import fs2.Stream

class Service(repo: Repository) extends Http4sDsl[IO] {

  val service = HttpService[IO] {
    case DELETE -> Root / "articles" / name =>
      repo.deleteArticle(name).flatMap { if (_) NoContent() else NotFound() }

    case POST -> Root / "articles" / name =>
      repo.createArticle(name).flatMap { if (_) NoContent() else Conflict() }

    case GET -> Root / "articles" / name => renderInventory(repo.getArticle(name))

    case req @ POST -> Root / "purchase" =>
      val changes: Stream[IO, Inventory] = for {
        purchase <- Stream.eval(req.decodeJson[Purchase])
        before <- repo.getInventory
        _ <- repo.updateStock(purchase.inventory)
        after <- repo.getInventory
      } yield diff(purchase.order.keys, before, after)
      renderInventory(changes)

    case req @ POST -> Root / "restock" =>
      val newState = for {
        purchase <- Stream.eval(req.decodeJson[Restock])
        _ <- repo.updateStock(purchase.inventory)
        inventory <- repo.getInventory
      } yield inventory
      renderInventory(newState)

    case GET -> Root / "inventory" =>
      getInventory
  }

  private def diff[A](keys: Iterable[A],
                           before: Map[A, Int],
                           after: Map[A, Int]): Map[A, Int] =
    keys.filter(before.contains).map { key =>
      key -> (before(key) - after(key))
    }.toMap

  private def getInventory: IO[Response[IO]] =
    renderInventory(repo.getInventory)

  private def renderInventory(inventory: Stream[IO, Inventory]) =
    Ok(inventory.map(_.asJson.noSpaces),
       `Content-Type`(MediaType.`application/json`))

}
