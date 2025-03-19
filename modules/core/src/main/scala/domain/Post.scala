package textboard.domain

import scala.concurrent.duration.FiniteDuration
import cats.*
import cats.implicits.*

case class InMemPost(id: Int, text: String, time: Long)

object InMemPost:
  given Show[InMemPost] with
    def show(p: InMemPost): String =
      s"\tPost no. ${p.id}\t${p.text}\t${p.time}\n"

case class Post(id: Int, text: String, time: Long, threadId: Int)

object Post:
  given Show[Post] with
    def show(p: Post): String =
      s"\tPost no. ${p.id}\t${p.text}\t${p.time}\n"

case class PostRequest(text: String)

object PostRequest:
  given Show[PostRequest] = _.text
