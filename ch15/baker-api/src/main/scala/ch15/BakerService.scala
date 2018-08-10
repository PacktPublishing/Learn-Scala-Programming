package ch15

import ch15.model._
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api._
import Service._

trait BakerService extends Service {
  def bake: ServiceCall[Source[RawCookies, NotUsed], Source[ReadyCookies, NotUsed]]

  override def descriptor: Descriptor = named("BakerService").withCalls(call(bake))
}
