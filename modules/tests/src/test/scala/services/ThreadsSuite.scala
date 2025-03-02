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
    yield expect(threadList.map(_.id) === List.range(0,100).reverse)
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
      threadList <- List.range(0,100).traverse(service.get)
      threadList2 <- service.getAll
    yield expect(threadList.map(_.get).size == threadList2.size)
  }




  //
  //
  //
  // object A {
  //   object B {
  //     object C {
  //       def test(a: Int) = a + 5
  //     }
  //   }
  // }
  //
  // pureTest("Simple expectations (success)") {
  //   val z = 15
  //
  //   expect(A.B.C.test(z) == z + 5)
  // }
  //
  // pureTest("Simple expectations (failure)") {
  //   val z = 15
  //
  //   expect(A.B.C.test(z) % 7 == 0)
  // }
  //
  //
  // pureTest("And/Or composition (success)") {
  //   expect(1 != 2) and expect(2 != 1) or expect(2 != 3)
  // }
  //
  // pureTest("And/Or composition (failure") {
  //   (expect(1 != 2) and expect(2 == 1)) or expect(2 == 3)
  // }
  //
  // pureTest("Varargs composition (success)") {
  //   // expect(1 + 1 == 2) && expect (2 + 2 == 4) && expect(4 * 2 == 8)
  //   expect.all(1 + 1 == 2, 2 + 2 == 4, 4 * 2 == 8)
  // }
  //
  // pureTest("Varargs composition (failure)") {
  //   // expect(1 + 1 == 2) && expect (2 + 2 == 4) && expect(4 * 2 == 8)
  //   expect.all(1 + 1 == 2, 2 + 2 == 5, 4 * 2 == 8)
  // }
  //
  // pureTest("Working with collections (success)") {
  //   forEach(List(1, 2, 3))(i => expect(i < 5)) and
  //     forEach(Option("hello"))(msg => expect.same(msg, "hello")) and
  //     exists(List("a", "b", "c"))(i => expect(i == "c")) and
  //     exists(Vector(true, true, false))(i => expect(i == false))
  // }
  //
  // pureTest("Working with collections (failure 1)") {
  //   forEach(Vector("hello", "world"))(msg => expect.same(msg, "hello"))
  // }
  //
  // pureTest("Working with collections (failure 2)") {
  //   exists(Option(39))(i => expect(i > 50))
  // }
  //
  // import cats.Eq
  // case class Test(d: Double)
  //
  // implicit val eqTest: Eq[Test] = Eq.by[Test, Double](_.d)
  //
  // pureTest("Strict equality (success)") {
  //   expect.eql("hello", "hello") and
  //     expect.eql(List(1, 2, 3), List(1, 2, 3)) and
  //     expect.eql(Test(25.0), Test(25.0))
  // }
  //
  // pureTest("Strict equality (failure 1)") {
  //   expect.eql("hello", "world")
  // }
  //
  // pureTest("Strict equality (failure 2)") {
  //   expect.eql(List(1, 2, 3), List(1, 19, 3))
  // }
  //
  // pureTest("Strict equality (failure 3)") {
  //   expect.eql(Test(25.0), Test(50.0))
  // }
  //
  // // Note that we don't have an instance of Eq[Hello]
  // // anywhere in scope
  // class Hello(val d: Double) {
  //   override def toString = s"Hello to $d"
  //
  //   override def equals(other: Any) =
  //     if(other != null && other.isInstanceOf[Hello])
  //       other.asInstanceOf[Hello].d == this.d
  //     else
  //       false
  // }
  //
  // pureTest("Relaxed equality comparison (success)") {
  //   expect.same(new Hello(25.0), new Hello(25.0))
  // }
  //
  // pureTest("Relaxed equality comparison (failure)") {
  //   expect.same(new Hello(25.0), new Hello(50.0))
  // }
  //
  // pureTest("Non macro-based expectations") {
  //   val condition : Boolean = false
  //   if (condition) success else failure("Condition failed")
  // }
  //
  // test("Failing fast expectations") {
  //   for {
  //     h <- IO.pure("hello")
  //     _ <- expect(h.isEmpty).failFast
  //   } yield success
  // }
