package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*

import textboard.domain.*

trait Posts[F[_]]:
  def get(id: Int, thread: Thread): F[Option[Post]]
  def create(text: String): F[Post]
  def getAll(thread: Thread): F[List[Post]]

object Posts:
  def inMemPosts[F[_]: Monad: Clock](
      counter: Ref[F, Int]
  ): Posts[F] = new Posts[F]:
    def get(id: Int, thread: Thread): F[Option[Post]] =
      thread.posts.find(_.id === id).pure

    def create(text: String): F[Post] =
      Clock[F].realTime
        .flatMap { time =>
          counter
            .modify { id =>
              val post = Post(id, text, time)
              (id + 1, post)
            }
        }

    def getAll(thread: Thread): F[List[Post]] =
      thread.posts.pure
