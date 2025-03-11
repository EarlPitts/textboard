package textboard

import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import cats.*
import cats.implicits.*
import com.comcast.ip4s._
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

  def run: IO[Unit] = for
    db <- Ref[IO].of(List.empty[Thread])
    threadCnt <- Ref[IO].of(0)
    postCnt <- Ref[IO].of(0)

    threads = Threads.inMemThreads(db, threadCnt)
    posts = Posts.inMemPosts(postCnt)

    httpApp = Router(
      "/" -> TextboardRoutes(threads, posts).httpRoutes
    ).orNotFound
    server = EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(httpApp)
      .build

    _ <- server.useForever
  yield ()
