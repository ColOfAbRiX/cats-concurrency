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

  def newContextShift(name: String, parallelism: Option[Int], priority: Option[Int]): ContextShift[IO] = {
    val threadFactory: ThreadFactory = new ThreadFactory {
      val threadCounter = new java.util.concurrent.atomic.AtomicInteger(0)

      val group = Option(System.getSecurityManager)
        .map(_.getThreadGroup)
        .getOrElse(Thread.currentThread.getThreadGroup)

      def newThread(runnable: Runnable): Thread = {
        val threadName = name + "-thread-" + threadCounter.getAndIncrement.toString
        val thread     = new Thread(group, runnable, threadName)
        thread.setPriority(priority.fold(Thread.NORM_PRIORITY)(identity))
        thread
      }
    }

    val threadPoolFactory = parallelism match {
      case None        => Executors.newCachedThreadPool(threadFactory)
      case Some(value) => Executors.newFixedThreadPool(value, threadFactory)
    }

    IO.contextShift(ExecutionContext.fromExecutor(threadPoolFactory))
  }
}

/*
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

object DefaultThreadFactory {
  private val poolNumber = new AtomicInteger(1)
}

class DefaultThreadFactory() extends ThreadFactory {
  private val threadNumber = new AtomicInteger(1)
  private val s: SecurityManager = System.getSecurityManager
  private val group = if ( s != null ) s.getThreadGroup else Thread.currentThread.getThreadGroup
  private val namePrefix = "pool-" + DefaultThreadFactory.poolNumber.getAndIncrement + "-thread-"

  def newThread(r: Runnable): Thread = {
    val t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement, 0)
    if (t.isDaemon)
      t.setDaemon(false)
    if (t.getPriority != Thread.NORM_PRIORITY)
      t.setPriority(Thread.NORM_PRIORITY)
    t
  }
}
 */
