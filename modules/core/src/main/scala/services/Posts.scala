package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import skunk.*

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
//
//   def inMemPosts[F[_]: Monad: Clock](
//       postgres: Resource[IO, Session[IO]]
//   ): Posts[F] = new Posts[F]:
//     def get(id: Int, tid: Int): F[Option[Post]] =
//       postgres.use { session =>
//         session -> ???
//       }
//     def create(text: String): F[Post] = for
//       id <- counter.get
//       time <- Clock[F].realTime
//       post = Post(id, text, time)
//       _ <- counter.update(_ + 1)
//     yield post
//     def getAll(thread: Thread): F[List[Post]] = Monad[F].pure(thread.posts)
//
// // object PostsSQL:
//
//
