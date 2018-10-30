package ch04

object TypeClasses {

  object OO {
    trait Cable {
      def connect(): Boolean
    }
    case class Usb(orientation: Boolean) extends Cable {
      override def connect(): Boolean = orientation
    }
    case class Lightning(length: Int) extends Cable {
      override def connect(): Boolean = length > 100
    }
    case class UsbC(kind: String) extends Cable {
      override def connect(): Boolean = kind.contains("USB 3.1")
    }
    def connectCable(c: Cable): Boolean = c.connect()
  }

  OO.connectCable(OO.Usb(false))
  OO.connectCable(OO.Lightning(150))


  object TC {

    case class Usb(orientation: Boolean)
    case class Lightning(length: Int)
    case class UsbC[Kind](kind: Kind)

    @scala.annotation.implicitNotFound("Cannot connect cable of type ${C}")
    trait Cable[C] {
      def connect(c: C): Boolean
    }
    implicit val UsbCable: Cable[Usb] = new Cable[Usb] {
      override def connect(c: Usb): Boolean = c.orientation
    }
    implicit val LightningCable: Cable[Lightning] = (_: Lightning).length > 100

    // compile error
    // implicit val UsbCCable: Cable[UsbC] = (c: UsbC) => c.kind.contains("USB 3.1")

    implicit val UsbCCableString: Cable[UsbC[String]] = (_: UsbC[String]).kind.contains("USB 3.1")

    def connectCable[C : Cable](c: C): Boolean = implicitly[Cable[C]].connect(c)

    import scala.language.reflectiveCalls

    implicit def usbCCableDelegate[T](implicit conn: T => Boolean): Cable[UsbC[T]] =
      (c: UsbC[T]) => conn(c.kind)

    implicit val symbolConnect: Symbol => Boolean = (_: Symbol).name.toLowerCase.contains("cable")

    implicit def adapt[A: Cable, B: Cable]: Cable[(A, B)] =
      (ab: (A, B)) => implicitly[Cable[A]].connect(ab._1) && implicitly[Cable[B]].connect(ab._2)

  }

  import ch04.TypeClasses.TC._
  connectCable(Usb(false))
  connectCable(Lightning(150))
  connectCable(UsbC("USB 3.1"))
  connectCable(UsbC('NonameCable))
  connectCable(UsbC('FakeKable))

/*
  Bad design, bad!

  implicit val isEven: Int => Boolean = i => i % 2 == 0
  implicit val hexChar: Char => Boolean = c => c >= 'A' && c <='F'

  connectCable(UsbC(10))
  connectCable(UsbC(11))
  connectCable(UsbC('D'))

*/

  val usb2usbC = (Usb(false), UsbC('NonameCable))
  connectCable(usb2usbC)
  val lightning2usbC = (Lightning(150), UsbC('NonameCable))
  connectCable(lightning2usbC)

  val usbC2usb2lightning2usbC = ((UsbC('NonameCable), Usb(false)), (Lightning(150), UsbC("USB 3.1")))
  connectCable(usbC2usb2lightning2usbC)

  val noUsbC_Long_Cable = (UsbC('NonameCable), (Lightning(150), UsbC(10L)))
  // connectCable(noUsbC_Long_Cable) // fails to compile

}

