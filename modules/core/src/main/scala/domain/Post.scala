package textboard.domain

import scala.concurrent.duration.FiniteDuration
import cats.*
import cats.implicits.*

case class Post(id: Int, text: String, time: Long)

object Post:
  given Show[Post] with
    def show(p: Post): String =
      s"\tPost no. ${p.id}\t${p.text}\t${p.time}\n"

case class PostRequest(text: String)

object PostRequest:
  given Show[PostRequest] = _.text
