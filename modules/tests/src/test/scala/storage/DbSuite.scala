package textboard.storage

import cats.effect.*
import cats.effect.std.*
import cats.implicits.*
import textboard.domain.*
import textboard.services.*

import weaver.*
import weaver.scalacheck.*
import org.scalacheck.*
import doobie.weaver.*
import doobie.*
import doobie.implicits.*

object DbSuite extends IOSuite with Checkers with IOChecker:

  val flushTables: List[Update0] =
    List(sql"post", sql"thread")
      .map(t => sql"DELETE FROM $t".update)

  type Res = Transactor[IO]
  override def sharedResource: Resource[IO, Res] =
    Resource
      .pure {
        val xa = Transactor.fromDriverManager[IO](
          driver = "org.sqlite.JDBC",
          url = "jdbc:sqlite:textboard.db",
          user = "",
          password = "",
          logHandler = None
        )
        // foreign key constraints are turned off in SQLite by default
        Transactor.before
          .modify(xa, sql"PRAGMA foreign_keys=ON".update.run >> _)
      }
      .evalTap { xa =>
        flushTables
          .traverse_(_.run.transact(xa))
      }

  val textGen = Gen.alphaNumStr

  test("Posting") { xa =>
    val p = Posts.mkPosts[IO](xa)
    val t = Threads.mkThreads[IO](xa)
    for
      tid <- t.create("test title", "test content")
      before <- p.getAll
      pid <- p.create("ize", tid)
      after <- p.getAll
    yield expect.all(
      before.size === 0,
      pid.isDefined,
      after.size === 1
    )
  }

  // test("Adding threads") { xa =>
  //   val t = Threads.mkThreads[IO](xa)
  //   for
  //     before <- t.getAll
  //     _ <- t.create("test title", "test content")
  //     _ <- t.create("test title", "test content")
  //     after <- t.getAll
  //   yield expect(after.size === before.size + 2)
  // }

  test("Posts queries") { implicit xa =>
    checkOutput(PostsSQL.selectById(0)) >>
      checkOutput(PostsSQL.selectAll) >>
      checkOutput(PostsSQL.create("", 0))
  }

  test("Threads queries") { implicit xa =>
    checkOutput(ThreadsSQL.selectById(0)) >>
      checkOutput(ThreadsSQL.selectAll) >>
      checkOutput(ThreadsSQL.create("", ""))
  }
