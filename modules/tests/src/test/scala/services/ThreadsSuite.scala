package services

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits._
import weaver.*

import textboard.domain.*
import textboard.services.*
import scala.concurrent.duration.*

object ThreadSuite extends SimpleIOSuite:

  val testPost = Post(1, "test post", 1.second)

  test("Counter is incremented") {
    val threadNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(threadNum)(service.create("Title", "Text")).sequence_
      count <- counter.get
    yield expect(count === threadNum)
  }

  test("Threads are added") {
    val threadNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(threadNum)(service.create("Title", "Text")).sequence_
      threadList <- threads.get
    yield expect(threadList.map(_.id) === List.range(0, 100).reverse)
  }

  test("Threads getAll gives back all threads") {
    val threadNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(threadNum)(service.create("Title", "Text")).sequence_
      threadList <- threads.get
      threadList2 <- service.getAll
    yield expect(threadList == threadList2)
  }

  test("Threads get by id works") {
    val threadNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(threadNum)(service.create("Title", "Text")).sequence_
      threadList <- List.range(0, 100).traverse(service.get)
      threadList2 <- service.getAll
    yield expect(threadList.map(_.get).size == threadList2.size)
  }

  test("Adding posts works") {
    val postNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(10)(service.create("Title", "Text")).sequence_
      _ <- List.range(0, 10).traverse_(service.add(testPost, _))
      threadList <- threads.get
    yield expect(threadList.forall(_.posts.head == testPost))
  }

  test("Adding posts to non-existing thread fails") {
    val postNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(10)(service.create("Title", "Text")).sequence_
      _ <- service.add(testPost, 100)
      threadList <- threads.get
    yield expect(threadList.forall(_.posts.isEmpty))
  }

  test("Thread with invalid id is not found") {
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- List.fill(10)(service.create("Title", "Text")).sequence_
      thread <- service.get(100)
    yield expect(thread.isEmpty)
  }

  test("Concurrent adding posts works") {
    val postNum = 100
    for
      counter <- Ref.of(0)
      threads <- Ref.of(List[Thread]())
      service = Threads.inMemThreads(threads, counter)
      _ <- service.create("Title", "Text")
      _ <- List.fill(postNum)(service.add(testPost, 0)).parSequence_
      t <- service.get(0)
    yield expect(t.get.posts.size == postNum)
  }

   test("Concurrent thread creation") {
    val threadNum = 100
     for
       counter <- Ref.of(0)
       threads <- Ref.of(List[Thread]())
       service = Threads.inMemThreads(threads, counter)
       _ <- List.fill(threadNum)(service.create("Title", "Text")).parSequence_
       threadList <- threads.get
     yield expect(threadList.size === threadNum)
   }
