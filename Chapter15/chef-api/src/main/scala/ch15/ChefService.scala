package ch15

import ch15.model._
import com.lightbend.lagom.scaladsl.api._
import Service._
import akka.Done
import com.lightbend.lagom.scaladsl.api.broker.Topic

trait ChefService extends Service {
  def mix: ServiceCall[Groceries, Done]

  def resultsTopic: Topic[Dough]

  override def descriptor: Descriptor = {
    named("ChefService")
      .withCalls(call(mix))
      .withTopics(
        topic(ChefService.ResultsTopic, resultsTopic)
      )
      .withAutoAcl(true)
  }
}
object ChefService {
  val ResultsTopic = "MixedResults"
}
