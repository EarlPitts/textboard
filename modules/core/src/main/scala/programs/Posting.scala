package textboard.programs

import cats.effect.*
import cats.effect.std.*
import cats.*
import cats.implicits.*

import textboard.domain.*
import textboard.services.*

def createThread[F[_]: Console: Monad](
    posts: Posts[F],
    threads: Threads[F]
): F[Int] = for
  title <- Console[F].readLine
  text <- Console[F].readLine
  id <- threads.create(title, text)
yield id

def addPost[F[_]: Console: Monad](
    posts: Posts[F],
    threads: Threads[F],
    id: Int
): F[Unit] = for
  text <- Console[F].readLine
  post <- posts.create(text)
  _ <- threads.add(post, id)
yield ()

def showBoard[F[_]: Console: Monad](threads: Threads[F]): F[Unit] =
  threads.getAll >>= (_.traverse_(Console[F].print))
