package textboard

import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import cats.*
import cats.implicits.*
import skunk.*
import skunk.implicits._
import skunk.codec.all._
import natchez.Trace.Implicits.noop

import textboard.domain.*
import textboard.programs.*
import textboard.services.*

// object AppWithDb extends IOApp.Simple:
//   val s: Resource[IO, Session[IO]] = Session.single(
//     host = "localhost",
//     port = 5432,
//     user = "postgres",
//     database = "textboard",
//     password = Option("pass")
//   )
//
//   def run: IO[Unit] = for
//     threadCnt <- Ref[IO].of(0)
//     postCnt <- Ref[IO].of(0)
//
//     threads = Threads.mkThreads(s, threadCnt)
//     posts = Posts.inMemPosts(postCnt)
//
//     _ <- program(threads, posts)
//   yield ()
//
//   def program[F[_]: Monad: Console](threads: Threads[F], posts: Posts[F]) = for
//     id <- createThread(posts, threads, "jozsi")
//     _ <- addPost(posts, threads, id)
//     _ <- addPost(posts, threads, id)
//     id2 <- createThread(posts, threads, "jozsi2")
//     _ <- addPost(posts, threads, id2)
//     _ <- showBoard(threads)
//   yield ()

object Main extends IOApp.Simple:
  def run: IO[Unit] = for
    db <- Ref[IO].of(List.empty[Thread])
    threadCnt <- Ref[IO].of(0)
    postCnt <- Ref[IO].of(0)

    threads = Threads.inMemThreads(db, threadCnt)
    posts = Posts.inMemPosts(postCnt)

    _ <- program(threads, posts)
  yield ()

  def program[F[_]: Monad: Console](threads: Threads[F], posts: Posts[F]) = for
    id <- createThread(posts, threads, "My first thread")
    _ <- addPost(posts, threads, id)
    _ <- addPost(posts, threads, id)
    id2 <- createThread(posts, threads, "My best thread")
    _ <- addPost(posts, threads, id2)
    _ <- showBoard(threads)
  yield ()
