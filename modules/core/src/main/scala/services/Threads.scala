package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import skunk._
import skunk.implicits._

import textboard.domain.*

trait Threads[F[_]]:
  def create(title: String, text: String): F[Int]
  def add(post: Post, id: Int): F[Unit]
  def get(id: Int): F[Option[Thread]]
  def getAll: F[List[Thread]]

object Threads:
  def inMemThreads[F[_]: Monad](
      threads: Ref[F, List[Thread]],
      counter: Ref[F, Int]
  ): Threads[F] = new Threads[F]:
    def create(title: String, text: String): F[Int] = for
      id <- counter.get
      thread = Thread(id, title, text, List())
      _ <- threads.update(thread :: _)
      _ <- counter.update(_ + 1)
    yield id

    def add(post: Post, id: Int): F[Unit] = for
      t <- get(id)
      _ <- t.fold(().pure) { t =>
        val updated = Thread(t.id, t.title, t.text, (post :: t.posts))
        threads.update(ts => updated :: ts.filter(_.id =!= id))
      }
    yield ()

    def get(id: Int): F[Option[Thread]] = threads.get.map(_.find(_.id === id))

    def getAll: F[List[Thread]] = threads.get

}
