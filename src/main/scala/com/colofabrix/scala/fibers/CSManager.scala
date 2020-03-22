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
/* JAVA
static class DefaultThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    DefaultThreadFactory() {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() :
                              Thread.currentThread().getThreadGroup();
        namePrefix = "pool-" +
                      poolNumber.getAndIncrement() +
                      "-thread-";
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r,
                              namePrefix + threadNumber.getAndIncrement(),
                              0);
        if (t.isDaemon())
            t.setDaemon(false);
        if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}
 */
