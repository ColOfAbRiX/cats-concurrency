package com.colofabrix.scala.fibers

import cats.effect._
import cats.implicits._

object Fibers extends IOApp {

  /** Creates a new IO with a random delay */
  def task(id: Int): IO[Unit] = IO {
    Thread.sleep(scala.util.Random.nextLong(500) + 250)
    println("%s - ID: %d".format(Thread.currentThread.getName, id))
  }

  /** Sequential running, no CS */
  def sequentialNoCs: IO[Unit] =
    IO(println("Sequential running, no CS")) >>
    task(1) *>
    task(2) *>
    task(3) *>
    task(4) *>
    IO(println(""))

  /** Sequential running with CS */
  def sequentialWithCs: IO[Unit] =
    IO(println("Sequential running with CS")) >>
    task(1) *> IO.shift *>
    task(2) *> IO.shift *>
    task(3) *> IO.shift *>
    task(4) *> IO.shift *>
    IO(println(""))

  /** Sequential running with CS on custom thread pool */
  def sequentialWithCsOnCustomPool: IO[Unit] =
    IO(println("Sequential running with CS on custom thread pool")) >>
    task(1) *> CSManager(3).shift *>
    task(2) *> CSManager(3).shift *>
    task(3) *> CSManager(3).shift *>
    task(4) *> CSManager(3).shift *>
    IO(println(""))

  /** Sequential running with CS back and forth on custom thread pool */
  def sequentialWithAlternatingCs: IO[Unit] =
    IO(println("Sequential running with CS back and forth on custom thread pool")) >>
    task(1) *> CSManager(2).shift *>
    task(2) *> IO.shift *>
    task(3) *> CSManager(2).shift *>
    task(4) *> IO.shift *>
    task(5) *> CSManager(2).shift *>
    task(6) *> IO.shift *>
    task(7) *> CSManager(2).shift *>
    task(8) *> IO.shift *>
    IO(println(""))

  /** Code that might look parallel but it's still sequential #1 */
  def sequentialNaughty1: IO[Unit] =
    IO(println("Code that might look parallel but it's still sequential #1")) >>
    IO {
      (task(1) *> IO.shift).unsafeRunSync()
      (task(2) *> IO.shift).unsafeRunSync()
      (task(3) *> IO.shift).unsafeRunSync()
      (task(4) *> IO.shift).unsafeRunSync()
    } *>
    IO(println(""))

  def fibersBlocked: IO[Unit] =
    for {
      f1 <- task(1).start
      f2 <- task(2).start
      f3 <- task(3).start
      f4 <- task(4).start
      _  <- f1.join
      _  <- f2.join
      _  <- f3.join
      _  <- f4.join
    } yield ()

  def run(args: List[String]): IO[ExitCode] =
    (for {
      // _ <- sequentialNoCs
      // _ <- sequentialWithCs
      // _ <- sequentialWithCsOnCustomPool
      // _ <- sequentialWithAlternatingCs
      // _ <- sequentialNaughty1
      _ <- fibersBlocked
    } yield ()) as ExitCode.Success

}
