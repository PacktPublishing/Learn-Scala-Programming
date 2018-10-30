package ch14

import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.{PersistentActor, RecoveryCompleted, SnapshotOffer}
import ch14.Commands.{GetArticle, GetInventory}

object InventoryActor {
  def props: Props = Props[InventoryActor]
  val persistenceId = "Inventory"
}

class InventoryActor extends PersistentActor with Actor with ActorLogging {

  override def persistenceId: String = InventoryActor.persistenceId

  private var inventory: Inventory = Inventory(Map.empty)

  override def receiveRecover: Receive = {
    case event: Event                          => inventory = inventory.update(event)
    case SnapshotOffer(_, snapshot: Inventory) => inventory = snapshot
    case RecoveryCompleted                     => saveSnapshot(inventory)
  }

  override def receiveCommand: Receive = {
    case GetInventory =>
      sender() ! inventory

    case GetArticle(name) =>
      sender() ! Inventory(inventory.state.filter(_._1 == name))

    case cmd: Command =>
      inventory.canUpdate(cmd) match {
        case None =>
          sender() ! None
        case Some(event) =>
          persistAsync(event) { ev =>
            inventory = inventory.update(ev)
            sender() ! Some(ev)
          }
      }

  }
}
