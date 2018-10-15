package ch14

import java.sql.SQLException

import cats.effect.IO
import ch14.Model.Inventory
import fs2.Stream
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor
import cats.implicits._

class Repository(transactor: Transactor[IO]) {

  def deleteArticle(name: String): IO[Boolean] = {
    sql"DELETE FROM article WHERE name = $name".update.run
      .transact(transactor)
      .map { affectedRows =>
        affectedRows == 1
      }
  }

  def createArticle(name: String): IO[Boolean] = {
    sql"INSERT INTO article (name, count) VALUES ($name, 0)".update.run.attempt
      .transact(transactor)
      .map {
        case Right(affectedRows) => affectedRows == 1
        case Left(_)             => false
      }
  }

  def updateStock(inventory: Inventory): Stream[IO, Either[Throwable, Unit]] = {
    val updates = inventory
      .map {
        case (name, count) =>
          sql"UPDATE article set count = count + $count where name = $name".update.run
      }
      .reduce(_ *> _)
    Stream
      .eval(FC.setAutoCommit(false) *> updates *> FC.setAutoCommit(true))
      .attempt
      .transact(transactor)
  }

  def getInventory: Stream[IO, Inventory] =
    queryToInventory(inventoryQuery)

  def getArticle(name: String): Stream[IO, Inventory] =
    queryToInventory( sql"SELECT name, count FROM article where name = $name")

  private val inventoryQuery: Fragment = sql"SELECT name, count FROM article"

  private def queryToInventory(query: Fragment) =
    query
      .query[(String, Int)]
      .stream
      .transact(transactor)
      .fold(Map.empty[String, Int])(_ + _)
}
