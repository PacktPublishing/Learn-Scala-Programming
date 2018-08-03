package ch14

import cats.effect.IO
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway

object DB {
  def transactor(config: DBConfig): IO[HikariTransactor[IO]] = {
    HikariTransactor.newHikariTransactor[IO](config.driver,
                                             config.url,
                                             config.user,
                                             config.password)
  }

  def initialize(transactor: HikariTransactor[IO]): IO[Unit] = {
    transactor.configure { dataSource =>
      IO {
        val flyWay = new Flyway()
        flyWay.setLocations("classpath:db_migrations")
        flyWay.setDataSource(dataSource)
        flyWay.migrate()
      }
    }
  }
}
