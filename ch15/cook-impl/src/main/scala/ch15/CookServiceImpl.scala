package ch15

import ch15.model._
import com.lightbend.lagom.scaladsl.api._

import scala.concurrent.Future

class CookServiceImpl extends CookService {
  override def cook: ServiceCall[Dough, RawCookies] = ServiceCall { dough =>
    Future.successful(RawCookies(makeCookies(dough.weight)))
  }
  private val cookieWeight = 60
  private def makeCookies(weight: Int): Int = weight / cookieWeight
}
