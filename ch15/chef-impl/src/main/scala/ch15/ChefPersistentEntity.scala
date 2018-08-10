package ch15

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import ch15.model.{Dough, Groceries}
import com.lightbend.lagom.scaladsl.persistence.{PersistentEntity, PersistentEntityRegistry}
import play.api.Logger

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

final class ChefPersistentEntity(persistentEntities: PersistentEntityRegistry,
                                 as: ActorSystem)
    extends PersistentEntity {

  private val logger = Logger("ChefEntity")

  implicit val ec: ExecutionContext = as.dispatcher
  lazy val thisEntity =
    persistentEntities.refFor[ChefPersistentEntity](this.entityId)

  override type Command = ChefCommand
  override type Event = ChefEvent
  override type State = ChefState

  override def initialState: ChefState = MixingState(Nil)

  private val mixingTime = 1.seconds

  private def dough(g: Groceries) = {
    import g._
    Dough(eggs * 50 + flour + sugar + chocolate)
  }

  override def behavior: Behavior =
    Actions()
      .onCommand[MixCommand, Done] {
        case (MixCommand(groceries), ctx, _) if groceries.eggs <= 0 =>
          ctx.invalidCommand(s"Need at least one egg but got: $groceries")
          ctx.done

        case (MixCommand(groceries), ctx, _) =>
          val id = UUID.randomUUID()
          logger.info(s"Mixing with id: $id")
          ctx.thenPersist(Mixing(id, groceries)) { evt =>
            as.scheduler.scheduleOnce(mixingTime)(
              thisEntity.ask(DoneCommand(id)))
            ctx.reply(Done)
          }
      }
      .onCommand[DoneCommand, Done] {
        case (DoneCommand(id), ctx, state) =>
          logger.info(s"DoneCommand with id: $id")
          state.batches
            .find(_.id == id)
            .map { g =>
              ctx.thenPersist(MixingDone(id, dough(g.groceries))) { _ =>
                logger.info(s"MixingDone with id: $id")
                ctx.reply(Done)
              }
            }
            .getOrElse(ctx.done)
      }
      .onEvent {
        case (m: Mixing, state) =>
          MixingState(state.batches :+ m)

        case (MixingDone(id, _), state) =>
          MixingState(state.batches.filterNot(_.id == id))
      }

}
