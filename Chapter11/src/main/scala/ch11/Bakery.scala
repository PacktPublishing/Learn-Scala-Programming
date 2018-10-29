package ch11

import akka.actor._
import akka.event.LoggingReceive
import akka.util.Timeout
import Cook.{LazyWorkerException, Dough, RawCookies}
import Mixer.{Groceries, MotorOverheatException, SlowRotationSpeedException, StrongVibrationException}
import Oven.{Cookies, Extract}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random
import akka.actor.SupervisorStrategy._

object Manager {
  trait State
  case object Idle extends State
  case object Shopping extends State
  case object Mixing extends State
  case object Forming extends State
  case object Baking extends State

  sealed trait Data
  case object Uninitialized extends Data

  final case class ShoppingList(eggs: Int,
                                flour: Int,
                                sugar: Int,
                                chocolate: Int)
    extends Data

  def shoppingList: ShoppingList = {
    val eggs = Random.nextInt(20) + 5
    ShoppingList(eggs, eggs * 50, eggs * 10, eggs * 5)
  }
}
import Manager._

class Manager extends FSM[State, Data] {

  private val idleTimeout = 1 seconds
  private val cook: ActorRef = context.actorOf(Props[Cook], "Cook")
  private val chef: ActorRef = context.actorOf(Props[Chef], "Chef")
  private val baker: ActorRef = context.actorOf(Props[Baker], "Baker")

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(s: ShoppingList, Uninitialized) ⇒
      goto(Shopping) forMax (5 seconds) using s
    case _ =>
      stay replying "Get back to work!"
  }

  when(Shopping) {
    case Event(g: Groceries, s: ShoppingList)
      if g.productIterator sameElements s.productIterator ⇒
      goto(Mixing) using g
    case Event(_: Groceries, _: ShoppingList) ⇒
      goto(Idle) using Uninitialized
  }

  when(Mixing) {
    case Event(p: Dough, _) ⇒
      goto(Forming) using p
  }

  when(Forming) {
    case Event(c: RawCookies, _) ⇒
      goto(Baking) using c
  }

  when(Baking, stateTimeout = idleTimeout * 20) {
    case Event(c: Cookies, _) ⇒
      log.info("Cookies are ready: {}", c)
      stay() replying "Thank you!"
    case Event(StateTimeout, _) =>
      goto(Idle) using Uninitialized
  }

  onTransition {
    case Idle -> Shopping ⇒
      val boy = sendBoy
      boy ! nextStateData
    case Shopping -> Idle =>
      self ! stateData
    case Shopping -> Mixing ⇒
      chef ! nextStateData
    case Mixing -> Forming =>
      cook ! nextStateData
    case Forming -> Baking =>
      baker ! nextStateData
    case Baking -> Idle =>
      self ! shoppingList

  }

  // optional
  whenUnhandled {
    case Event(e, s) ⇒
      log.warning("Unhandled {} in state {}/{}", e, stateName, s)
      stay
  }

  setTimer("Anxiety", shoppingList, idleTimeout, repeat = false)
  initialize()

  private def sendBoy: ActorRef = {
    val store = "akka.tcp://Store@127.0.0.1:2553"
    val seller = context.actorSelection(s"$store/user/Seller")
    val boyProps = Boy.props(seller)
    context.actorOf(boyProps)
  }
}
object Boy {
  def props(seller: ActorSelection): Props = Props(classOf[Boy], seller)
}
class Boy(seller: ActorSelection) extends Actor {
  override def receive: Receive = {
    case s: ShoppingList =>
      seller forward s
      self ! PoisonPill
  }
}
object Mixer {
  final case class Groceries(eggs: Int, flour: Int, sugar: Int, chocolate: Int)
    extends Data
  def props: Props = Props[Mixer].withDispatcher("mixers-dispatcher")
  class MotorOverheatException extends Exception
  class SlowRotationSpeedException extends Exception
  class StrongVibrationException extends Exception
}

class Mixer extends Actor with ActorLogging {
  override def receive: Receive = {
    case Groceries(eggs, flour, sugar, chocolate) =>
      val rnd = Random.nextInt(10)
      if (rnd == 0) {
        log.info("Motor Overheat")
        throw new MotorOverheatException
      }
      if (rnd < 3) {
        log.info("Slow Speed")
        throw new SlowRotationSpeedException
      }
      Thread.sleep(3000)
      sender() ! Dough(eggs * 50 + flour + sugar + chocolate)
  }
}

class Chef extends Actor with ActorLogging with Stash {
  private implicit val timeout: Timeout = Timeout(5 seconds)
  private var manager: ActorRef = _
  private var message: Groceries = _
  override def receive: Receive = {
    case Groceries(eggs, flour, sugar, chocolate) =>
      manager = sender()
      for (i <- 1 to eggs) {
        val mixer = context.watch(context.actorOf(Mixer.props, s"Mixer_$i"))
        message = Groceries(1, flour / eggs, sugar / eggs, chocolate / eggs)
        import akka.pattern.ask
        val job = (mixer ? message).mapTo[Dough]
        import akka.pattern.pipe
        import context.dispatcher
        job.pipeTo(self)
      }
      log.info("Sent jobs to {} mixers", eggs)
      context.become(waitingForResults(eggs, 0), discardOld = false)
    case Terminated(child) =>
      log.debug(s"Terminated: $child")
  }
  def waitingForResults(mixers: Int, weight: Int): Receive = {
    case g: Groceries => stash()
    case p: Dough =>
      if (mixers <= 1) {
        manager ! Dough(weight)
        context.children.foreach(context.stop)
        unstashAll()
        context.unbecome()
        log.info("Ready to accept new mixing jobs")
      } else {
        context.become(waitingForResults(mixers - 1, weight + p.weight),
          discardOld = true)
      }
    case Terminated(child) =>
      log.debug(s"Terminated: $child")

  }
  override val supervisorStrategy: OneForOneStrategy =
    OneForOneStrategy(maxNrOfRetries = 10, withinTimeRange = 1 minute) {
      case _: MotorOverheatException ⇒
        self ! Dough(0)
        Stop
      case _: SlowRotationSpeedException ⇒
        sender() ! message
        Restart
      case _: StrongVibrationException =>
        sender() ! message
        Resume
      case _: Exception ⇒ Escalate
    }
}

object Baker {
  import scala.concurrent.duration._
  private val defaultBakingTime = 2.seconds
}
class Baker(bakingTime: FiniteDuration) extends Actor {
  def this() = this(Baker.defaultBakingTime)
  private val oven: ActorRef = context.actorOf(Oven.props(Oven.size), "Oven")
  private var queue = 0
  private var timer: Option[Cancellable] = None
  override def receive: Receive = {
    case RawCookies(count) =>
      queue += count
      if (sender() != oven && timer.isEmpty) timer = sendToOven()
    case c: Cookies =>
      context.parent ! c
      assert(timer.nonEmpty)
      if (queue > 0) timer = sendToOven() else timer = None
  }
  private def sendToOven() = {
    oven ! RawCookies(queue)
    queue = 0
    import context.dispatcher
    Option(context.system.scheduler.scheduleOnce(bakingTime, oven, Extract))
  }
  override def postStop(): Unit = {
    timer.foreach(_.cancel())
    super.postStop()
  }
}

object Cook {
  final case class Dough(weight: Int) extends Data
  final case class RawCookies(count: Int) extends Data
  class LazyWorkerException extends Throwable
}

class Cook extends Actor with ActorLogging {
  override def receive: Receive = {
    case Dough(weight) =>
      if (Random.nextInt(5) == 0) {
        log.info("Laziness attack")
        throw new LazyWorkerException
      }
      val numberOfCookies = makeCookies(weight)
      sender() ! RawCookies(numberOfCookies)
  }
  private val cookieWeight = 30
  private def makeCookies(weight: Int): Int = weight / cookieWeight
}
object Oven {
  final case object Extract
  final case class Cookies(count: Int)
  def props(size: Int) = Props(classOf[Oven], size)
  val size = 12
}
class Oven(size: Int) extends Actor {
  private var cookiesInside = 0
  override def receive = LoggingReceive {
    case RawCookies(count) => insert(count).foreach(sender().!)
    case Extract           => sender() ! Cookies(extract())
  }

  def insert(count: Int): Option[RawCookies] =
    if (cookiesInside > 0) {
      Some(RawCookies(count))
    } else {
      val tooMany = math.max(0, count - size)
      cookiesInside = math.min(size, count)
      Some(tooMany).filter(_ > 0).map(RawCookies)
    }

  def extract(): Int = {
    val cookies = cookiesInside
    cookiesInside = 0
    cookies
  }
}
class GuardianSupervisorStrategyConfigurator
  extends SupervisorStrategyConfigurator {
  override def create(): SupervisorStrategy = AllForOneStrategy() {
    case _: LazyWorkerException ⇒
      println("Lazy workers. Let's try again with another crew!")
      Restart
  }
}
object Bakery extends App {
  val bakery = ActorSystem("Bakery")
  val manager: ActorRef =
    bakery.actorOf(Props[Manager], "Manager")
}
