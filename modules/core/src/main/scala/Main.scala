package textboard

import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import cats.*
import cats.implicits.*
import com.comcast.ip4s._
import doobie.*
import doobie.implicits.*
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server
import org.http4s.implicits.*

import textboard.domain.*
import textboard.programs.*
import textboard.services.*
import textboard.http.*
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger

object Main extends IOApp.Simple:
  implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run: IO[Unit] =
    val xa = Transactor.fromDriverManager[IO](
      driver = "org.sqlite.JDBC",
      url = "jdbc:sqlite:textboard.db",
      user = "",
      password = "",
      logHandler = None
    )

    val threads = Threads.mkThreads(xa)
    val posts = Posts.mkPosts(xa)

    val httpApp = Router(
      "/" -> TextboardRoutes(threads, posts).httpRoutes
    ).orNotFound
    val server = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build

    server.useForever
