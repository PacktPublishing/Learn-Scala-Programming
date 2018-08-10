package ch15

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry
import com.lightbend.lagom.scaladsl.server._
import com.softwaremill.macwire._
import play.api.libs.ws.ahc.AhcWSComponents


abstract class ChefApplication(context: LagomApplicationContext)
  extends LagomApplication(context) with CassandraPersistenceComponents with LagomKafkaComponents {
  override lazy val lagomServer: LagomServer = serverFor[ChefService](wire[ChefServiceImpl])
  override lazy val jsonSerializerRegistry = new JsonSerializerRegistry {
    override def serializers = ChefModel.serializers
  }
}

class ChefLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new ChefApplication(context) with AhcWSComponents {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new ChefApplication(context) with AhcWSComponents with LagomDevModeComponents

}
