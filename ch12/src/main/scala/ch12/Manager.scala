package ch12

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.receptionist.Receptionist.{Find, Listing}
import akka.actor.typed.scaladsl.Behaviors
import akka.util.Timeout
import ch12.Bakery.{Groceries, Dough, RawCookies, ReadyCookies}
import ch12.Boy.GoShopping
import ch12.Shop.{SellByList, ShoppingList}

import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

object Manager {
  private val idleTimeout = 20.seconds
  private val Anxiety = 'Anxiety

  sealed trait Command
  final case object StartBaking extends Command
  final case class OneSeller(seller: ActorRef[SellByList]) extends Command
  final case object NoSeller extends Command
  final case class ReceiveGroceries(groceries: Groceries) extends Command
  final case class ReceiveDough(dough: Dough) extends Command
  final case class ReceiveRawCookies(cookies: RawCookies) extends Command
  final case class ReceiveReadyCookies(cookies: ReadyCookies) extends Command

  val openBakery: Behavior[Command] = Behaviors.setup { context =>
    context.log.info("Opening Bakery")
    val mixerBuilder = Mixer.mix(3.seconds)
    val chef = context.spawn(Chef.idle(mixerBuilder), "Chef")
    val cook = context.spawn(Cook.form, "Cook")
    val baker = context.spawn(Baker.turnOvenOn, "Baker")

    Behaviors.withTimers { timers =>
      timers.startPeriodicTimer(Anxiety, StartBaking, idleTimeout)
      manage(chef, cook, baker)
    }
  }

  def manage(chef: ActorRef[Chef.Command],
             cook: ActorRef[Cook.FormCookies],
             baker: ActorRef[Baker.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      def lookupSeller: Behavior[Command] = Behaviors.receiveMessagePartial {
        case StartBaking =>
          implicit val lookupTimeout: Timeout = 1.second
          context.ask(context.system.receptionist)(Find(Shop.SellerKey)) {
            case Success(listing: Listing) =>
              listing
                .serviceInstances(Shop.SellerKey)
                .headOption
                .map { seller =>
                  OneSeller(seller)
                }
                .getOrElse {
                  NoSeller
                }
            case Failure(_) =>
              NoSeller
          }
          manage(chef, cook, baker)
      }

      def sendBoyShopping: Behavior[Command] = Behaviors.receiveMessagePartial {
        case OneSeller(seller) =>
          val boy = context.spawn(Boy.goShopping, "Boy")
          boy ! GoShopping(shoppingList, seller, context.self)
          context.log.info("Go shopping to {}", seller)
          Behaviors.same
        case NoSeller =>
          context.log.error("Seller could not be found")
          Thread.sleep(1000)
          Behaviors.same
      }
      def waitingForGroceries: Behavior[Command] =
        Behaviors.receiveMessagePartial[Command] {
          case ReceiveGroceries(g) =>
            context.log.info("Mixing {}", g)
            chef ! Chef.Mix(g, context.self)
            manage(chef, cook, baker)
        }
      def waitingForDough: Behavior[Command] =
        Behaviors.receiveMessagePartial[Command] {
          case ReceiveDough(p) =>
            context.log.info("Forming {}", p)
            cook ! Cook.FormCookies(p, context.self)
            manage(chef, cook, baker)
        }
      def waitingForRawCookies: Behavior[Command] =
        Behaviors.receiveMessagePartial[Command] {
          case ReceiveRawCookies(c) =>
            context.log.info("Baking {}", c)
            baker ! Baker.BakeCookies(c, context.self)
            manage(chef, cook, baker)
        }
      def waitingForReadyCookies: Behavior[Command] =
        Behaviors.receiveMessagePartial[Command] {
          case ReceiveReadyCookies(c) =>
            context.log.info("Done baking cookies: {}", c)
            manage(chef, cook, baker)
        }

      lookupSeller orElse
        sendBoyShopping orElse
        waitingForGroceries orElse
        waitingForDough orElse
        waitingForRawCookies orElse
        waitingForReadyCookies

    }

  def shoppingList: ShoppingList = {
    val eggs = Random.nextInt(10) + 5
    ShoppingList(eggs, eggs * 50, eggs * 10, eggs * 5)
  }
}
