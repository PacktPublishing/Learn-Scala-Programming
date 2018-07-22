package ch13

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge}

/**
  * This balancer implementation is taken from the akka cookbook,
  * see https://doc.akka.io/docs/akka/2.5.13/stream/stream-cookbook.html
  */
object Balancer {
  def apply[In, Out](subFlow: Flow[In, Out, Any],
                     count: Int): Flow[In, Out, NotUsed] = {

    Flow.fromGraph(createGraph(subFlow, count))
  }

  import akka.stream.scaladsl.GraphDSL
  import GraphDSL.Implicits._

  def createGraph[Out, In](subFlow: Flow[In, Out, Any], count: Int) = {
    val balanceBlock  = Balance[In](count, waitForAllDownstreams = false)
    val mergeBlock = Merge[Out](count, eagerComplete = false)
    GraphDSL.create() { implicit builder ⇒
      val balancer = builder.add(balanceBlock)
      val merge = builder.add(mergeBlock)

      for (_ ← 1 to count) balancer ~> subFlow ~> merge

      FlowShape(balancer.in, merge.out)
    }
  }
}
