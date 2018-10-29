package ch15

object model {
  final case class ShoppingList(eggs: Int, flour: Int, sugar: Int, chocolate: Int)
  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)
  final case class Dough(weight: Int)
  final case class RawCookies(count: Int) {
    def +(c: RawCookies): RawCookies = RawCookies(count + c.count)
  }
  final case class ReadyCookies(count: Int)

  import play.api.libs.json._

  implicit val dough: Format[Dough] = Json.format
  implicit val rawCookies: Format[RawCookies] = Json.format
  implicit val readyCookies: Format[ReadyCookies] = Json.format
  implicit val groceries: Format[Groceries] = Json.format
  implicit val shoppingList: Format[ShoppingList] = Json.format
}
