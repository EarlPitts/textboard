package textboard.http

import cats.*
import cats.implicits.*
import cats.effect.*
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.dsl.Http4sDsl
import org.http4s.circe.*
import scala.concurrent.duration.FiniteDuration
import io.circe.*
import io.circe.syntax.*
import io.circe.generic.auto.*

import textboard.services.Threads
import textboard.services.Posts
import textboard.domain.*

case class TextboardRoutes[F[_]: Monad: JsonDecoder](
    threads: Threads[F],
    posts: Posts[F]
) extends Http4sDsl[F]:
  val httpRoutes = HttpRoutes.of[F]:

    case GET -> Root / "thread" / IntVar(id) =>
      threads
        .get(id)
        .flatMap {
          case None         => NotFound()
          case Some(thread) => Ok(thread.asJson)
        }

    case req @ POST -> Root / "thread" / "add" =>
      for
        payload <- req.asJsonDecode[ThreadRequest]
        id <- threads.create(payload.title, payload.text)
        resp <- Created(id.asJson)
      yield resp

    case req @ POST -> Root / "post" / "add" / IntVar(id) =>
      for
        payload <- req.asJsonDecode[PostRequest]
        post <- posts.create(payload.text)
        res <- threads.add(post, id)
        resp <- res match
          case None     => NotFound()
          case Some(id) => Created(id.asJson)
      yield resp
