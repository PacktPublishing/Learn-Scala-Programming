package ch04

object TypeClassVariance {

  trait Cable[C] {
    def connect(c: C): Boolean
  }

  abstract class UsbConnector
  case class Usb(orientation: Boolean) extends UsbConnector
  case class Lightning(length: Int) extends UsbConnector
  case class UsbC[Kind](kind: Kind) extends UsbConnector

  implicit val usbCable: Cable[UsbConnector] = new Cable[UsbConnector] {
    override def connect(c: UsbConnector): Boolean = {
      println(s"Connecting $c")
      true
    }
  }

  implicit def usbPolyCable[T <: UsbConnector]: Cable[T] = new Cable[T] {
    override def connect(c: T): Boolean = {
      println(s"Poly-Connecting $c")
      true
    }
  }

  implicit val usbCCable: Cable[UsbC[String]] = new Cable[UsbC[String]] {
    override def connect(c: UsbC[String]): Boolean = {
      println(s"Connecting USB C ${c.kind}")
      true
    }
  }

  def connectCable[C : Cable](c: C): Unit = implicitly[Cable[C]].connect(c)

  connectCable(UsbC("3.1"))

  // implicitly[Cable[UsbConnector] <:< Cable[UsbC[String]]]
}





/*



sealed trait Mode
case class Usb2(version: String) extends Mode
case class Usb3(version: String) extends Mode
case object PowerDelivery extends Mode
case object Alternate extends Mode
case object AudioAdapterAccessory extends Mode

class UsbC[M <: Mode](val mode: M)

class UsbC3(override val mode: Usb3) extends UsbC[Usb3](mode)


implicit def usbC[M <: Mode]: Cable[UsbC[M]] = (_: UsbC[M]).mode.non

def connectCable[C: Cable](c: C): Boolean = implicitly[Cable[C]].connect(c)
*/
