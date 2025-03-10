package textboard

import cats.effect.*
import cats.effect.kernel.*
import cats.effect.std.*
import cats.*
import cats.implicits.*
import com.comcast.ip4s._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import org.http4s.server.Router
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Server

import textboard.domain.*
import textboard.programs.*
import textboard.services.*
import textboard.http.*

implicit val loggerFactory: LoggerFactory[IO] = Slf4jFactory.create[IO]

object Main extends IOApp.Simple:
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
