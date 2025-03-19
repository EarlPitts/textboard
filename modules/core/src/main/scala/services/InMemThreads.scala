package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*

import textboard.domain.*

trait InMemThreads[F[_]]:
  def create(title: String, text: String): F[Int]
  def add(post: InMemPost, id: Int): F[Option[Int]]
  def get(id: Int): F[Option[InMemThread]]
  def getAll: F[List[InMemThread]]

object InMemThreads:
  def mkThreads[F[_]: Monad](
      threads: Ref[F, List[InMemThread]]
  ): InMemThreads[F] = new InMemThreads[F]:
    def create(title: String, text: String): F[Int] =
      threads.modify { ts =>
        val id = ts.size
        val thread = InMemThread(ts.size, title, text, List())
        (thread :: ts, id)
      }

    def add(post: InMemPost, id: Int): F[Option[Int]] =
      threads.modify { ts =>
        ts.find(_.id === id) match
          case Some(t) => {
            val updated = InMemThread(t.id, t.title, t.text, post :: t.posts)
            (updated :: ts.filter(_.id =!= id), Some(post.id))
          }
          case None => (ts, None)
      }

    def get(id: Int): F[Option[InMemThread]] = threads.get.map(_.find(_.id === id))

    def getAll: F[List[InMemThread]] = threads.get
