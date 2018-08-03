package ch14

import org.flywaydb.core.Flyway

object DB {
  def initialize(cfg: DBConfig): Int = {
    val flyWay = new Flyway()
    flyWay.setDataSource(cfg.url, cfg.user, cfg.password)
    flyWay.migrate()
  }
}
