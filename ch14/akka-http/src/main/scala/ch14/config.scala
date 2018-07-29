package ch14

import java.util.concurrent.TimeUnit

import akka.util.Timeout
import com.typesafe.config
import com.typesafe.config.{ConfigFactory, ConfigResolveOptions}

case class ServerConfig(host: String, port: Int)

case class DBConfig(driver: String, url: String, user: String, password: String)

case class Config(server: ServerConfig, database: DBConfig, timeout: Timeout)

object Config {
  def load(): Config = {
    val c = ConfigFactory.parseResources("application.conf").resolve()
    val srv = c.getConfig("server")
    val serverConfig = ServerConfig(srv.getString("host"), srv.getInt("port"))
    val db = c.getConfig("slick.db")
    val dbConfig = DBConfig(db.getString("driver"),
                            db.getString("url"),
                            db.getString("user"),
                            db.getString("password"))
    val d = c.getDuration("timeout")
    val timeout = new Timeout(d.toMillis, TimeUnit.MILLISECONDS)
    Config(serverConfig, dbConfig, timeout)
  }
}
