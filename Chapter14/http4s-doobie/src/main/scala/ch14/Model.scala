package ch14

object Model {
  type Inventory = Map[String, Int]

  abstract sealed class Operation(val inventory: Inventory)

  final case class Purchase(order: Inventory)
      extends Operation(order.mapValues(_ * -1))

  final case class Restock(override val inventory: Inventory)
      extends Operation(inventory)

}
