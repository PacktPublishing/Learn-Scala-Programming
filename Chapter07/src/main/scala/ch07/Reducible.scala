package ch07

trait Reducible[A] {

  @throws("UnsupportedOperationException")
  def reduceLeft[B >: A](op: (B, A) => B): B

  @throws("UnsupportedOperationException")
  def reduceRight[B >: A](op: (A, B) => B): B

  @throws("UnsupportedOperationException")
  def reduce[B >: A](op: (B, B) => B): B = reduceLeft(op)

  def reduceLeftOption[B >: A](op: (B, A) => B): Option[B]

  def reduceRightOption[B >: A](op: (A, B) => B): Option[B]

  def reduceOption[B >: A](op: (B, B) => B): Option[B] = reduceLeftOption(op)

}
