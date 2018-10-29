package ch14

import cats.effect.IO
import com.typesafe.config.ConfigFactory
import pureconfig.error.ConfigReaderException

case class ServerConfig(host: String, port: Int)

case class DBConfig(driver: String, url: String, user: String, password: String)

case class Config(server: ServerConfig, database: DBConfig)

object Config {
  def load(fileName: String): IO[Config] = {
    IO {
      val config = ConfigFactory.load(fileName)
      pureconfig.loadConfig[Config](config)
    }.flatMap {
      case Left(e) =>
        IO.raiseError[Config](new ConfigReaderException[Config](e))
      case Right(config) =>
        IO.pure(config)
    }
  }
}
