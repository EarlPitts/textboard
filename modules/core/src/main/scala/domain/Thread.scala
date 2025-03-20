package textboard.domain

import cats.*
import cats.implicits.*

case class InMemThread(
    id: Int,
    title: String,
    text: String,
    posts: List[InMemPost]
) // extends AnyVal

object InMemThread:
  given Show[InMemThread] with
    def show(t: InMemThread): String =
      s"------------------------------------------------------\n" ++
        s"Thread no. ${t.id} ${t.title}\n${t.text}\n${t.posts.map(_.show).mkString("\n")}"

case class Thread(
    id: Int,
    title: String,
    text: String,
    time: Long,
    posts: List[Post]
) // extends AnyVal

object Thread:
  given Show[Thread] with
    def show(t: Thread): String =
      s"------------------------------------------------------\n" ++
        s"Thread no. ${t.id} ${t.title}\n${t.text}\n${t.posts.map(_.show).mkString("\n")}"

case class ThreadRequest(title: String, text: String)

object ThreadRequest:
  given Show[ThreadRequest] = _.title
