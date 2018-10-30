package ch15

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.transport.Method

trait ManagerService extends Service {
  def bake(count: Int): ServiceCall[NotUsed, Done]
  def sell(count: Int): ServiceCall[NotUsed, Int]
  def report: ServiceCall[NotUsed, Int]

  override def descriptor: Descriptor = {
    import Service._
    named("Bakery").withCalls(
      restCall(Method.POST, "/bake/:count", bake _),
      restCall(Method.POST, "/sell?count", sell _),
      pathCall("/report", report)
    )
  }
}
