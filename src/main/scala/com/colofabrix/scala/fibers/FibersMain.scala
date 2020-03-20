package com.colofabrix.scala.fibers

import cats.effect._
import cats.implicits._
import java.util.concurrent._
import scala.concurrent._

object CSManager {
  private val threadPools = scala.collection.mutable.HashMap.empty[Int, ContextShift[IO]]

  def apply(nThreads: Int): ContextShift[IO] = {
    def executor: Executor = Executors.newFixedThreadPool(
      nThreads,
      new ThreadFactory {
        val ctr = new java.util.concurrent.atomic.AtomicInteger(0)
        def newThread(r: Runnable): Thread = {
          val back = new Thread(r, s"thread-${ctr.getAndIncrement()}")
          back.setDaemon(true)
          back
        }
      },
    )
    lazy val newContextShift = IO.contextShift(ExecutionContext.fromExecutor(executor))
    threadPools.getOrElseUpdate(nThreads, newContextShift)
  }
}

object Fibers extends IOApp {

  /** Creates a new IO with a random delay */
  def task(id: Int): IO[Unit] = IO {
    Thread.sleep(scala.util.Random.nextLong(500) + 500)
    println("%s - ID: %d".format(Thread.currentThread.getName, id))
  }

  /** Sequential running, no CS */
  def sequentialNoCs: IO[Unit] =
    IO(println("Sequential running, no CS")) *>
    task(1) *>
    task(2) *>
    task(3) *>
    task(4) *>
    IO(println(""))

  /** Sequential running with CS */
  def sequentialWithCs: IO[Unit] =
    IO(println("Sequential running with CS")) *>
    IO.shift *> task(1) *>
    IO.shift *> task(2) *>
    IO.shift *> task(3) *>
    IO.shift *> task(4) *>
    IO(println(""))

  /** Sequential running with CS on custom thread pool */
  def sequentialWithCsOnCustomPool: IO[Unit] =
    IO(println("Sequential running with CS on custom thread pool")) *>
    CSManager(3).shift *> task(1) *>
    CSManager(3).shift *> task(2) *>
    CSManager(3).shift *> task(3) *>
    CSManager(3).shift *> task(4) *>
    IO(println(""))

  /** Sequential running with CS back and forth on custom thread pool */
  def sequentialWithAlternatingCs: IO[Unit] =
    IO(println("Sequential running with CS back and forth on custom thread pool")) *>
    CSManager(2).shift *> task(1) *>
    IO.shift *> task(2) *>
    CSManager(2).shift *> task(3) *>
    IO.shift *> task(4) *>
    CSManager(2).shift *> task(5) *>
    IO.shift *> task(6) *>
    CSManager(2).shift *> task(7) *>
    IO.shift *> task(8) *>
    IO(println(""))

  /** Code that might look parallel but it's still sequential #1 */
  def sequentialNaughty1: IO[Unit] =
    IO(println("Code that might look parallel but it's still sequential #1")) *>
    IO {
      (IO.shift *> task(1)).unsafeRunSync()
      (IO.shift *> task(2)).unsafeRunSync()
      (IO.shift *> task(3)).unsafeRunSync()
      (IO.shift *> task(4)).unsafeRunSync()
    } *>
    IO(println(""))

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- sequentialNoCs
      _ <- sequentialWithCs
      _ <- sequentialWithCsOnCustomPool
      _ <- sequentialWithAlternatingCs
      _ <- sequentialNaughty1
    } yield ()
  }.as(ExitCode.Success)

}
