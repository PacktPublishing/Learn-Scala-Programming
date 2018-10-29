package ch07

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

trait MonoidFoldable[A, F[_]] {

  def foldRight(as: F[A]): A
  def foldLeft(as: F[A]): A
  def foldBalanced(as: F[A]): A
  def foldPar(as: F[A])(implicit ec: ExecutionContext): Future[A]
}

object MonoidFoldable {

  implicit def listMonoidFoldable[A : Monoid]: MonoidFoldable[A, List] = new MonoidFoldable[A, List] {
    private val m = implicitly[Monoid[A]]
    override def foldRight(as: List[A]): A = as.foldRight(m.identity)(m.op)

    override def foldLeft(as: List[A]): A = as.foldLeft(m.identity)(m.op)

    override def foldBalanced(as: List[A]): A = as match {
      case Nil => m.identity
      case List(one) => one
      case _ => val (l, r) = as.splitAt(as.length/2)
        m.op(foldBalanced(l), foldBalanced(r))
    }

    private val parallelLimit = 10
    override def foldPar(as: List[A])(implicit ec: ExecutionContext): Future[A] = {
      if (as.length < parallelLimit) Future(foldBalanced(as))
      else {
        val (l, r) = as.splitAt(as.length/2)
        Future.reduceLeft(List(foldPar(l), foldPar(r)))(m.op)
      }
    }
  }
}
