package com.colofabrix.scala.fibers

import cats.effect._
import cats.implicits._

object Fibers extends IOApp {

  /** Creates a new IO with a random delay */
  def task(id: Int): IO[Unit] = IO {
    Thread.sleep(scala.util.Random.nextLong(1000))
    println("%s - ID: %d".format(Thread.currentThread.getName, id))
  }

  /** Sequential running, no CS */
  def sequentialNoCs: IO[Unit] =
    IO(println("Sequential running, no CS")) >>
    task(1) *>
    task(2) *>
    task(3) *>
    task(4) *>
    task(5) >>
    IO(println(""))

  /** Sequential running with CS */
  def sequentialWithCs: IO[Unit] =
    IO(println("Sequential running with CS")) >>
    IO.shift *> task(1) *>
    IO.shift *> task(2) *>
    IO.shift *> task(3) *>
    IO.shift *> task(4) *>
    IO.shift *> task(5) >>
    IO(println(""))

  /** Sequential running with CS on custom thread pool */
  def sequentialWithCsOnCustomPool: IO[Unit] =
    IO(println("Sequential running with CS on custom thread pool")) >>
    CSManager(3).shift *> task(1) *>
    CSManager(3).shift *> task(2) *>
    CSManager(3).shift *> task(3) *>
    CSManager(3).shift *> task(4) *>
    CSManager(3).shift *> task(5) >>
    IO(println(""))

  /** Sequential running with CS back and forth on custom thread pool */
  def sequentialWithAlternatingCs: IO[Unit] =
    IO(println("Sequential running with CS back and forth on custom thread pool")) >>
    CSManager(2).shift *> task(1) *>
    IO.shift           *> task(2) *>
    CSManager(2).shift *> task(3) *>
    IO.shift           *> task(4) *>
    CSManager(2).shift *> task(5) *>
    IO.shift           *> task(6) *>
    CSManager(2).shift *> task(7) *>
    IO.shift           *> task(8) >>
    IO(println(""))

  /** Code that might look parallel but it's still sequential #1 */
  def sequentialDodgy: IO[Unit] =
    IO(println("Code that might look parallel but it's still sequential #1")) >>
    IO {
      (IO.shift *> task(1)).unsafeRunSync()
      (IO.shift *> task(2)).unsafeRunSync()
      (IO.shift *> task(3)).unsafeRunSync()
      (IO.shift *> task(4)).unsafeRunSync()
      (IO.shift *> task(5)).unsafeRunSync()
    } >>
    IO(println(""))

  /** Parallel running in the default CS */
  def parallelNoCs: IO[Unit] =
    IO(println("Parallel execution in the default CS")) >>
    (for {
      f1 <- task(1).start
      f2 <- task(2).start
      f3 <- task(3).start
      f4 <- task(4).start
      f5 <- task(4).start
      _  <- f1.join
      _  <- f2.join
      _  <- f3.join
      _  <- f5.join
    } yield ()) >>
    IO(println(""))

  /** Parallel running with CS on custom thread pool */
  def parallelWithCsOnCustomPool: IO[Unit] =
    IO(println("Parallel running with CS on custom thread pool")) >>
    (for {
      f1 <- task(1).start(CSManager(2))
      f2 <- task(2).start(CSManager(2))
      f3 <- task(3).start(CSManager(2))
      f4 <- task(4).start(CSManager(2))
      f5 <- task(5).start(CSManager(2))
      _  <- f1.join
      _  <- f2.join
      _  <- f3.join
      _  <- f4.join
      _  <- f5.join
    } yield ()) >>
    IO(println(""))

  /** Parallel running with CS back and forth on custom thread pool */
  def parallelWithAlternatingCs: IO[Unit] =
    IO(println("Parallel running with CS back and forth on custom thread pool")) >>
    (for {
      f1 <- task(1).start(CSManager(2))
      f2 <- task(2).start
      f3 <- task(3).start(CSManager(2))
      f4 <- task(4).start
      f5 <- task(5).start(CSManager(2))
      f6 <- task(6).start
      _  <- f1.join
      _  <- f2.join
      _  <- f3.join
      _  <- f4.join
      _  <- f5.join
      _  <- f6.join
    } yield ()) >>
    IO(println(""))

  /** Applicative's parallel running with no CS */
  def parMapNoCs: IO[Unit] =
    IO(println("Applicative's parallel running with no CS")) >>
    (
      task(1),
      task(2),
      task(3),
      task(4),
      task(5),
      task(6),
    ).parMapN { (_, _, _, _, _, _) =>
      ()
    } >>
    IO(println(""))

  /** Applicative's parallel running with CS on custom thread pool */
  def parMapWithCsOnCustomPool: IO[Unit] =
    IO(println("Applicative's parallel running with CS on custom thread pool")) >>
    (
      CSManager(2).shift *> task(1),
      CSManager(2).shift *> task(2),
      CSManager(2).shift *> task(3),
      CSManager(2).shift *> task(4),
      CSManager(2).shift *> task(5),
      CSManager(2).shift *> task(6),
    ).parMapN { (_, _, _, _, _, _) =>
      ()
    } >>
    IO(println(""))

  /** Applicative's parallel running with CS back and forth on custom thread pool */
  def parMapWithAlternatingCs: IO[Unit] =
    IO(println("Applicative's parallel running with CS back and forth on custom thread pool")) >>
    (
      CSManager(2).shift *> task(1),
      IO.shift           *> task(2),
      CSManager(2).shift *> task(3),
      IO.shift           *> task(4),
      CSManager(2).shift *> task(5),
      IO.shift           *> task(6),
    ).parMapN { (_, _, _, _, _, _) =>
      ()
    } >>
    IO(println(""))

  def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- IO(println("SEQUENTIAL\n"))
      _ <- sequentialNoCs
      _ <- sequentialWithCs
      _ <- sequentialWithCsOnCustomPool
      _ <- sequentialWithAlternatingCs
      _ <- sequentialDodgy
      _ <- IO(println("FIBERS\n"))
      _ <- parallelNoCs
      _ <- parallelWithCsOnCustomPool
      _ <- parallelWithAlternatingCs
      _ <- IO(println("APPLICATIVE\n"))
      _ <- parMapWithCsOnCustomPool
      _ <- parMapWithCsOnCustomPool
      _ <- parMapWithAlternatingCs
    } yield ()) as ExitCode.Success

}
