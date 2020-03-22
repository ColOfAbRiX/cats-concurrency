package com.colofabrix.scala.fibers

import cats.effect._
import java.util.concurrent._
import scala.concurrent._

object CSManager {
  private[this] val threadPools = scala.collection.mutable.HashMap.empty[Int, ContextShift[IO]]

  def apply(nThreads: Int): ContextShift[IO] = {
    lazy val threadFactory = new ThreadFactory {
      val threadNumber                   = new java.util.concurrent.atomic.AtomicInteger(0)
      def newThread(r: Runnable): Thread = new Thread(r, s"thread-${threadNumber.getAndIncrement()}")
    }

    lazy val newContextShift = IO.contextShift(
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(nThreads, threadFactory),
      ),
    )

    threadPools.getOrElseUpdate(nThreads, newContextShift)
  }
}
