package textboard.programs

import cats.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits.*
import weaver.*
import scala.concurrent.duration.*

import textboard.domain.*
import textboard.services.*
import textboard.programs.*
import java.nio.charset.Charset
import cats.effect.testkit.TestControl

object PostingSuite extends SimpleIOSuite:

  implicit val consoleInstance: Console[IO] = new Console[IO]:
    def error[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def errorln[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def println[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def print[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def readLineWithCharset(charset: Charset): IO[String] = "testInput".pure

  protected class testConsole(output: Ref[IO, List[String]])
      extends Console[IO] {
    def error[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def errorln[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def println[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def print[A](a: A)(implicit S: Show[A]): IO[Unit] =
      output.update(a.show :: _)
    def readLineWithCharset(charset: Charset): IO[String] = "testInput".pure
  }

  test("createThread creates valid threads") {
    val threadNum = 10
    for
      counter <- Ref.of(0)
      threadsList <- Ref.of(List.empty[InMemThread])
      postsService = InMemPosts.mkPosts(counter)
      threadsService = InMemThreads.mkThreads(threadsList)
      _ <- createThread(postsService, threadsService).parReplicateA_(threadNum)
      threads <- threadsService.getAll
    yield expect(threads.map(_.id) === List.range(0, 10).reverse)
  }

  test("addPost adds the post to the right thread") {
    val threadNum = 10
    val threadId = 5
    for
      counter <- Ref.of(0)
      threadsList <- Ref.of(List.empty[InMemThread])
      postsService = InMemPosts.mkPosts(counter)
      threadsService = InMemThreads.mkThreads(threadsList)
      _ <- createThread(postsService, threadsService).parReplicateA_(threadNum)
      _ <- addPost(postsService, threadsService, threadId)
      thread <- threadsService.get(threadId)
      otherThreads <- threadsService.getAll.map(_.filter(_.id =!= threadId))
    yield expect(thread.exists(t => t.posts.size === 1)) and
      expect(otherThreads.forall(_.posts.size === 0))
  }

  test("showBoard correctly renders the board") {
    val expected =
      """------------------------------------------------------
        |Thread no. 0 testInput
        |testInput
        |        Post no. 0      testInput       0             
        |------------------------------------------------------
        |Thread no. 1 testInput
        |testInput
        |        Post no. 1      testInput       0             
        """.stripMargin.replaceAll("\\s", "")
    TestControl.executeEmbed {
      for
        outputRef <- Ref.of(List.empty[String])
        c = testConsole(outputRef)
        counter <- Ref.of(0)
        threadsList <- Ref.of(List.empty[InMemThread])
        postsService = InMemPosts.mkPosts(counter)
        threadsService = InMemThreads.mkThreads(threadsList)
        id1 <- createThread(postsService, threadsService)
        id2 <- createThread(postsService, threadsService)
        _ <- addPost(postsService, threadsService, id1)
        _ <- addPost(postsService, threadsService, id2)
        _ <- showBoard(threadsService)(using c, effect)
        output <- outputRef.get
        _ = println(output)
      yield expect(output.mkString.replaceAll("\\s", "") === expected)
    }
  }
