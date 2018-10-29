package ch15

import java.util.UUID

import akka.Done
import ch15.model.{Dough, Groceries}
import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer


sealed trait ChefCommand
final case class MixCommand(groceries: Groceries) extends ChefCommand with ReplyType[Done]
final case class DoneCommand(id: UUID) extends ChefCommand with ReplyType[Done]

sealed trait ChefEvent
final case class Mixing(id: UUID, groceries: Groceries) extends ChefEvent
final case class MixingDone(id: UUID, dough: Dough) extends ChefEvent with AggregateEvent[MixingDone] {
  override def aggregateTag: AggregateEventTag[MixingDone] = ChefModel.EventTag
}

sealed trait ChefState {
  def batches: List[Mixing]
}
final case class MixingState(batches: List[Mixing])
  extends ChefState

object ChefModel {
  val EventTag: AggregateEventTag[MixingDone] = AggregateEventTag[MixingDone]("MixingDone")
  import play.api.libs.json._
  implicit val mixingFormat: OFormat[Mixing] = Json.format[Mixing]
  val serializers = List(
    JsonSerializer(mixingFormat),
    JsonSerializer(Json.format[MixingDone]),
    JsonSerializer(Json.format[MixingState]))
}
