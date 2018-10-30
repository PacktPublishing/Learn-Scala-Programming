package ch02

object InfixTypes {
  import Linearization._
  import scala.language.higherKinds

  type Or[A, B]
  type And[A, B]
  type +=[A, B] = Or[A, B]
  type =:[A, B] = And[A, B]

  type CC = Or[And[A, B], C]
  type DA = A =: B =: C
  type DB = A And B And C
  // type E = A += B =: C // wrong associativity
  type F = (A += B) =: C

  type |[A, B] = Or[A, B]
  type &[A, B] = And[A, B]
  type G = A & B | C
}
