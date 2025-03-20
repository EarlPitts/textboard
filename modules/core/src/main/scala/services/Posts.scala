package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import doobie.*
import doobie.implicits.*

import textboard.domain.*

trait Posts[F[_]]:
  def get(id: Int): F[Option[Post]]
  def create(content: String, threadId: Int): F[Option[Int]]
  def getAll: F[List[Post]]

object Posts:
  def mkPosts[F[_]: Async](xa: Transactor[F]): Posts[F] = new Posts[F]:
    def create(content: String, threadId: Int): F[Option[Int]] =
      PostsSQL
        .create(content, threadId)
        .option
        .transact(xa)

    def get(id: Int): F[Option[Post]] =
      PostsSQL
        .selectById(id)
        .option
        .transact(xa)

    def getAll: F[List[Post]] =
      PostsSQL
        .selectAll
        .to[List]
        .transact(xa)

object PostsSQL:
  def selectById(id: Int): Query0[Post] =
    sql"select * from post where id = $id".query[Post]

  def selectAll: Query0[Post] =
    sql"select * from post".query[Post]

  def create(content: String, threadId: Int): Query0[Int] =
    sql"""
      insert into post (content, threadid)
      values ($content, $threadId)
      returning id"""
      .query[Int]
