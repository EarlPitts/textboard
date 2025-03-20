package textboard.services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import doobie.*
import doobie.implicits.*

import textboard.domain.*

trait Threads[F[_]]:
  def create(title: String, content: String): F[Int]
  def get(id: Int): F[Option[Thread]]
  def getAll: F[List[Thread]]

object Threads:
  def mkThreads[F[_]: Async](xa: Transactor[F]): Threads[F] = new Threads[F]:
    def create(title: String, content: String): F[Int] =
      ThreadsSQL
        .create(title, content)
        .unique
        .transact(xa)

    def get(id: Int): F[Option[Thread]] =
      ThreadsSQL
        .selectById(id)
        .to[List]
        .map {
          case Nil => None
          case rs =>
            val ps = rs.flatMap(_._2)
            rs.headOption
              .map { (t, _) =>
                Thread(t.id, t.title, t.content, t.timestamp, ps)
              }
        }
        .transact(xa)

    def getAll: F[List[Thread]] =
      ThreadsSQL.selectAll
        .to[List]
        .map { rs =>
          rs.groupBy(_._1).map { (_, rs) =>
            val ps = rs.flatMap(_._2)
            val (t, _) = rs.head
            Thread(t.id, t.title, t.content, t.timestamp, ps)
          }
        }
        .transact(xa)
        .map(_.toList)

object ThreadsSQL:
  case class ThreadRecord(
      id: Int,
      title: String,
      content: String,
      timestamp: Long
  )

  def selectById(
      threadId: Int
  ): Query0[(ThreadRecord, Option[Post])] =
    sql"""
      SELECT t.id, t.title, t.content, t.time, p.id, p.content, p.time, p.threadid
      FROM thread t
      LEFT JOIN post p ON t.id = p.threadid
      WHERE t.id = $threadId"""
      .query[(ThreadRecord, Option[Post])]

  def selectAll: Query0[(ThreadRecord, Option[Post])] =
    sql"""
      SELECT t.id, t.title, t.content, t.time, p.id, p.content, p.time, p.threadid
      FROM thread t
      LEFT JOIN post p ON t.id = p.threadid"""
      .query[(ThreadRecord, Option[Post])]

  def create(title: String, content: String): Query0[Int] =
    sql"""
      insert into thread (title, content)
      values ($title, $content)
      returning id"""
      .query[Int]
