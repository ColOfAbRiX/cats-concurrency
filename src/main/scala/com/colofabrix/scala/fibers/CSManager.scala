package com.colofabrix.scala.fibers

import cats.effect._
import java.util.concurrent._
import scala.concurrent._

object CSManager {
  private[this] val threadPools = scala.collection.mutable.HashMap.empty[Int, ContextShift[IO]]

  def apply(nThreads: Int): ContextShift[IO] = {
    lazy val threadFactory: ThreadFactory = new ThreadFactory {
      val group = Option(System.getSecurityManager)
        .map(_.getThreadGroup)
        .getOrElse(Thread.currentThread.getThreadGroup)

      val threadCounter = new java.util.concurrent.atomic.AtomicInteger(0)

      def newThread(r: Runnable): Thread = new Thread(r, s"thread-${threadCounter.getAndIncrement()}")
    }

    lazy val newContextShift = IO.contextShift(
      ExecutionContext.fromExecutor(
        Executors.newFixedThreadPool(nThreads, threadFactory),
      ),
    )

    threadPools.getOrElseUpdate(nThreads, newContextShift)
  }
}
