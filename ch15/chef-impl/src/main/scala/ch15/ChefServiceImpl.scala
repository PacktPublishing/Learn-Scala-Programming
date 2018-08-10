package ch15

import akka.Done
import akka.actor.ActorSystem
import ch15.model._
import com.lightbend.lagom.scaladsl.api._
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{
  EventStreamElement,
  PersistentEntityRegistry
}
import com.softwaremill.macwire.wire
import play.api.Logger

import scala.concurrent.Future

class ChefServiceImpl(persistentEntities: PersistentEntityRegistry,
                      as: ActorSystem)
    extends ChefService {

  private lazy val entity = wire[ChefPersistentEntity]
  persistentEntities.register(entity)

  private val logger = Logger("Chef")

  override def mix: ServiceCall[Groceries, Done] = ServiceCall { groceries =>
    val ref = persistentEntities.refFor[ChefPersistentEntity]("Chef")
    logger.info(s"PersistentEntity: $ref")
    val reply: Future[MixCommand#ReplyType] = ref.ask(MixCommand(groceries))
    reply
  }

  override def resultsTopic: Topic[Dough] =
    TopicProducer.singleStreamWithOffset { fromOffset =>
      persistentEntities
        .eventStream(ChefModel.EventTag, fromOffset)
        .map { ev =>
          logger.info(s"Event: $ev")
          (convertEvent(ev), ev.offset)
        }
    }

  private def convertEvent(chefEvent: EventStreamElement[ChefEvent]): Dough = {
    chefEvent.event match {
      case MixingDone(_, gr) => gr
    }
  }
}
