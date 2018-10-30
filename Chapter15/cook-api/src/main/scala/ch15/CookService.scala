package ch15

import ch15.model._
import com.lightbend.lagom.scaladsl.api._

trait CookService extends Service {
  def cook: ServiceCall[Dough, RawCookies]

  override def descriptor: Descriptor = {
    import Service._
    named("CookService").withCalls(call(cook))
  }
}
