package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*

import textboard.domain.*

trait Threads[F[_]]:
  def create(title: String, text: String): F[Int]
  def add(post: Post, id: Int): F[Option[Int]]
  def get(id: Int): F[Option[Thread]]
  def getAll: F[List[Thread]]

object Threads:
  def inMemThreads[F[_]: Monad](
      threads: Ref[F, List[Thread]],
      counter: Ref[F, Int]
  ): Threads[F] = new Threads[F]:
    def create(title: String, text: String): F[Int] =
      threads.modify { ts =>
        val id = ts.size
        val thread = Thread(ts.size, title, text, List())
        (thread :: ts, id)
      }

    def add(post: Post, id: Int): F[Option[Int]] =
      threads.modify { ts =>
        ts.find(_.id === id) match
          case Some(t) => {
            val updated = Thread(t.id, t.title, t.text, post :: t.posts)
            (updated :: ts.filter(_.id =!= id), Some(post.id))
          }
          case None => (ts, None)
      }

    def get(id: Int): F[Option[Thread]] = threads.get.map(_.find(_.id === id))

    def getAll: F[List[Thread]] = threads.get
