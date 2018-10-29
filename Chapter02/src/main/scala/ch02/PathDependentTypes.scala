package ch02

object PathDependentTypes {

  final case class Lock() {

    final case class Key()

    def open(key: Key): Lock = this

    def close(key: Key): Lock = this

    def openWithMaster(key: Lock#Key): Lock = this

    def makeKey: Key = new Key

    def makeMasterKey: Lock#Key = new Key
  }

  val blue: Lock = Lock()
  val red: Lock = Lock()
  val blueKey: blue.Key = blue.makeKey
  val anotherBlueKey: blue.Key = blue.makeKey
  val redKey: red.Key = red.makeKey

  blue.open(blueKey)
  blue.open(anotherBlueKey)
  // blue.open(redKey) // compile error
  // red.open(blueKey) // compile error

  val masterKey: Lock#Key = red.makeMasterKey

  blue.openWithMaster(masterKey)
  red.openWithMaster(masterKey)

}
