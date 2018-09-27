package ch06

object Effects extends App {

  type User
  type Subscription
  type Fee

  import java.util

  val map = new util.HashMap[String, Int] {
    put("zero", 0)
  }

  val list = new util.ArrayList[String] {
    add("zero")
  }

  println(map.get("zero"))
  println(list.get(0))

  println(map.get("one"))

  list.get(1) match {
    case null => println("Not found")
    case notNull => println(notNull)
  }

}
