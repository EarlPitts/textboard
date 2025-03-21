package services

import cats.*
import cats.implicits.*
import cats.effect.*
import weaver.*

import textboard.domain.*
import textboard.services.*
import scala.concurrent.duration.*

object ThreadSuite extends SimpleIOSuite:

  val testPost = InMemPost(1, "test post", 1)

  test("Threads are added") {
    val threadNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA_(threadNum)
      threadList <- threads.get
    yield expect(threadList.map(_.id) === List.range(0, 100).reverse)
  }

  test("Threads getAll gives back all threads") {
    val threadNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA(threadNum)
      threadList <- threads.get
      threadList2 <- service.getAll
    yield expect(threadList == threadList2)
  }

  test("Threads get by id works") {
    val threadNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA(threadNum)
      threadList <- List.range(0, 100).traverse(service.get)
      threadList2 <- service.getAll
    yield expect(threadList.map(_.get).size == threadList2.size)
  }

  test("Adding posts works") {
    val postNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA_(10)
      ids <- List.range(0, 10).traverse(service.add(testPost, _))
      threadList <- threads.get
    yield expect(threadList.forall(_.posts.head == testPost)) and
      expect(ids.forall(_.isDefined))
  }

  test("Adding posts to non-existing thread fails") {
    val postNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA_(postNum)
      id <- service.add(testPost, 100)
      threadList <- threads.get
    yield expect(threadList.forall(_.posts.isEmpty)) and
      expect(id.isEmpty)
  }

  test("Thread with invalid id is not found") {
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text").replicateA_(10)
      thread <- service.get(100)
    yield expect(thread.isEmpty)
  }

  test("Concurrent adding posts works") {
    val postNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      _ <- service.create("Title", "Text")
      _ <- service.add(testPost, 0).parReplicateA_(postNum)
      t <- service.get(0)
    yield expect(t.get.posts.size == postNum)
  }

  test("Concurrent thread creation") {
    val threadNum = 100
    for
      threads <- Ref.of(List[InMemThread]())
      service = InMemThreads.mkThreads(threads)
      ids <- service.create("Title", "Text").parReplicateA(threadNum)
      threadList <- threads.get
    yield expect(threadList.map(_.id).reverse === List.range(0, threadNum)) and
      expect(ids.sorted === List.range(0, threadNum))
  }
