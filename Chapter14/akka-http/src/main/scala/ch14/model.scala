package ch14

import ch14.Commands.{
  CreateArticle,
  DeleteArticle,
  PurchaseArticles,
  RestockArticles
}
import ch14.Events.{
  ArticleCreated,
  ArticleDeleted,
  ArticlesPurchased,
  ArticlesRestocked
}
import stamina.Persistable

sealed trait Event extends Persistable

object Events {
  final case class ArticleCreated(name: String, count: Int) extends Event
  final case class ArticleDeleted(name: String) extends Event
  final case class ArticlesPurchased(order: Map[String, Int]) extends Event
  final case class ArticlesRestocked(stock: Map[String, Int]) extends Event
}

sealed trait Command
sealed trait Query

object Commands {
  final case class CreateArticle(name: String, count: Int) extends Command
  final case class DeleteArticle(name: String) extends Command
  final case class PurchaseArticles(order: Map[String, Int]) extends Command
  final case class RestockArticles(stock: Map[String, Int]) extends Command
  final case object GetInventory extends Query
  final case class GetArticle(name: String) extends Query

}

final case class Inventory(state: Map[String, Int]) extends Persistable {
  def plus(name: String, count: Int): Option[Inventory] =
    state.get(name) match {
      case None => Some(Inventory(state.updated(name, count)))
      case _    => None
    }
  def minus(name: String): Option[Inventory] =
    if (state.contains(name))
      Some(Inventory(state.filterKeys(k => !(k == name))))
    else None
  def add(o: Map[String, Int]): Inventory = {
    val newState = state.foldLeft(Map.empty[String, Int]) {
      case (acc, (k, v)) =>
        acc.updated(k, v + o.getOrElse(k, 0))
    }
    Inventory(newState)
  }

  def canUpdate(cmd: Command): Option[Event] = cmd match {
    case CreateArticle(name, cnt) =>
      plus(name, cnt).map(_ => ArticleCreated(name, cnt))
    case DeleteArticle(name)     => minus(name).map(_ => ArticleDeleted(name))
    case PurchaseArticles(order) =>
      val updated = add(order.mapValues(_ * -1))
      if (updated.state.forall(_._2 >=  0)) Some(ArticlesPurchased(order)) else None
    case RestockArticles(stock)  => Some(ArticlesRestocked(stock))
  }
  def update(event: Event): Inventory = event match {
    case ArticleCreated(name, cnt) => plus(name, cnt).get
    case ArticleDeleted(name)      => minus(name).get
    case ArticlesPurchased(order)  => add(order.mapValues(_ * -1))
    case ArticlesRestocked(stock)  => add(stock)
  }
}
