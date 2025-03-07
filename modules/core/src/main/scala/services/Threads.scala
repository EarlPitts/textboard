package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*

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
    def create(title: String, text: String): F[Int] =
      counter
        .modify { id =>
          val thread = Thread(id, title, text, List())
          (id + 1, thread)
        }
        .flatMap { thread =>
          threads
            .update(thread :: _)
            .as(thread.id)
        }

    def add(post: Post, id: Int): F[Unit] = threads.update { ts =>
      ts.find(_.id === id) match
        case Some(t) => {
          val updated = Thread(t.id, t.title, t.text, post :: t.posts)
          updated :: ts.filter(_.id =!= id)
        }
        case None => ts
    }

    def get(id: Int): F[Option[Thread]] = threads.get.map(_.find(_.id === id))

    def getAll: F[List[Thread]] = threads.get
