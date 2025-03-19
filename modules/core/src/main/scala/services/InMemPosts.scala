package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*

import textboard.domain.*

trait InMemPosts[F[_]]:
  def get(id: Int, thread: InMemThread): F[Option[InMemPost]]
  def create(text: String): F[InMemPost]
  def getAll(thread: InMemThread): F[List[InMemPost]]

object InMemPosts:
  def inMemPosts[F[_]: Monad: Clock](
      counter: Ref[F, Int]
  ): InMemPosts[F] = new InMemPosts[F]:
    def get(id: Int, thread: InMemThread): F[Option[InMemPost]] =
      thread.posts.find(_.id === id).pure

    def create(text: String): F[InMemPost] =
      Clock[F].realTime
        .flatMap { time =>
          counter
            .modify { id =>
              val post = InMemPost(id, text, time.toSeconds)
              (id + 1, post)
            }
        }

    def getAll(thread: InMemThread): F[List[InMemPost]] =
      thread.posts.pure
