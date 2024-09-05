package textboard.programs
//
// import scala.concurrent.duration._
// import scala.util.control.NoStackTrace

import cats.*
import cats.implicits.*
import cats.effect.*
import cats.effect.std.Console
import cats.implicits._
import weaver.*

import textboard.domain.*
import textboard.services.*
import java.nio.charset.Charset
import scala.concurrent.duration.*

object PostingSuite extends SimpleIOSuite:

  implicit val noOp: Console[IO] = new Console[IO]:
    def println[A](a: A)(implicit S: Show[A]): IO[Unit] = IO.unit
    override def readLine: IO[String] = IO.pure("sajt")
    def error[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def errorln[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def print[A](a: A)(implicit S: Show[A]): IO[Unit] = ???
    def readLineWithCharset(charset: Charset): IO[String] = ???

  // implicit val testClock: Clock[IO] = new Clock[IO]:
  //   def applicative: Applicative[IO] = ???
  //   def monotonic: IO[FiniteDuration] = ???
  //   override def realTime: IO[FiniteDuration] = IO.pure(1.second)

  val successfulPosts: Posts[IO] =
    new TestPosts:
      override def create(text: String): IO[Post] =
        IO.pure(Post(1, text, 1.second))

  val emptyThreads: Threads[IO] =
    new TestThreads:
      override def create(title: String, post: Post): IO[Int] =
        IO.pure(1)
      override def getAll: IO[List[Thread]] = IO(List.empty)
      override def get(id: Int): IO[Option[Thread]] = IO(None)

  test("creating thread works"):
    createThread[IO](successfulPosts, emptyThreads).map((id: Int) =>
        expect.same(id, 1))

//   val retryPolicy: RetryPolicy[IO] = limitRetries[IO](MaxRetries)
//
//   def successfulClient(paymentId: PaymentId): PaymentClient[IO] =
//     new PaymentClient[IO] {
//       def process(payment: Payment): IO[PaymentId] =
//         IO.pure(paymentId)
//     }
//
//   val unreachableClient: PaymentClient[IO] =
//     new PaymentClient[IO] {
//       def process(payment: Payment): IO[PaymentId] =
//         IO.raiseError(PaymentError(""))
//     }
//
//   def recoveringClient(attemptsSoFar: Ref[IO, Int], paymentId: PaymentId): PaymentClient[IO] =
//     new PaymentClient[IO] {
//       def process(payment: Payment): IO[PaymentId] =
//         attemptsSoFar.get.flatMap {
//           case n if n === 1 => IO.pure(paymentId)
//           case _            => attemptsSoFar.update(_ + 1) *> IO.raiseError(PaymentError(""))
//         }
//     }
//
//   val failingOrders: Orders[IO] = new TestOrders {
//     override def create(
//         userId: UserId,
//         paymentId: PaymentId,
//         items: NonEmptyList[CartItem],
//         total: Money
//     ): IO[OrderId] =
//       IO.raiseError(OrderError(""))
//   }
//
//   val emptyCart: ShoppingCart[IO] = new TestCart {
//     override def get(userId: UserId): IO[CartTotal] =
//       IO.pure(CartTotal(List.empty, USD(0)))
//   }
//
//   def failingCart(cartTotal: CartTotal): ShoppingCart[IO] = new TestCart {
//     override def get(userId: UserId): IO[CartTotal] =
//       IO.pure(cartTotal)
//     override def delete(userId: UserId): IO[Unit] = IO.raiseError(new NoStackTrace {})
//   }
//
//   def successfulCart(cartTotal: CartTotal): ShoppingCart[IO] = new TestCart {
//     override def get(userId: UserId): IO[CartTotal] =
//       IO.pure(cartTotal)
//     override def delete(userId: UserId): IO[Unit] = IO.unit
//   }
//
//   def successfulOrders(orderId: OrderId): Orders[IO] = new TestOrders {
//     override def create(
//         userId: UserId,
//         paymentId: PaymentId,
//         items: NonEmptyList[CartItem],
//         total: Money
//     ): IO[OrderId] =
//       IO.pure(orderId)
//   }
//
//   val gen = for {
//     uid <- userIdGen
//     pid <- paymentIdGen
//     oid <- orderIdGen
//     crt <- cartTotalGen
//     crd <- cardGen
//   } yield (uid, pid, oid, crt, crd)
//
//   implicit val bg = TestBackground.NoOp
//   implicit val lg = NoOpLogger[IO]
//
//   test("empty cart") {
//     forall(gen) {
//       case (uid, pid, oid, _, card) =>
//         Checkout[IO](successfulClient(pid), emptyCart, successfulOrders(oid), retryPolicy)
//           .process(uid, card)
//           .attempt
//           .map {
//             case Left(EmptyCartError) => success
//             case _                    => failure("Cart was not empty as expected")
//           }
//     }
//   }
//
//   test("unreachable payment client") {
//     forall(gen) {
//       case (uid, _, oid, ct, card) =>
//         Ref.of[IO, Option[GivingUp]](None).flatMap { retries =>
//           implicit val rh = TestRetry.givingUp(retries)
//
//           Checkout[IO](unreachableClient, successfulCart(ct), successfulOrders(oid), retryPolicy)
//             .process(uid, card)
//             .attempt
//             .flatMap {
//               case Left(PaymentError(_)) =>
//                 retries.get.map {
//                   case Some(g) => expect.same(g.totalRetries, MaxRetries)
//                   case None    => failure("expected GivingUp")
//                 }
//               case _ => IO.pure(failure("Expected payment error"))
//             }
//         }
//     }
//   }
//
//   test("failing payment client succeeds after one retry") {
//     forall(gen) {
//       case (uid, pid, oid, ct, card) =>
//         (Ref.of[IO, Option[WillDelayAndRetry]](None), Ref.of[IO, Int](0)).tupled.flatMap {
//           case (retries, cliRef) =>
//             implicit val rh = TestRetry.recovering(retries)
//
//             Checkout[IO](
//               recoveringClient(cliRef, pid),
//               successfulCart(ct),
//               successfulOrders(oid),
//               retryPolicy
//             ).process(uid, card)
//               .attempt
//               .flatMap {
//                 case Right(id) =>
//                   retries.get.map {
//                     case Some(w) =>
//                       expect.same(id, oid) |+| expect.same(0, w.retriesSoFar)
//                     case None => failure("Expected one retry")
//                   }
//                 case Left(_) => IO.pure(failure("Expected Payment Id"))
//               }
//         }
//     }
//   }
//
//   test("cannot create order, run in the background") {
//     forall(gen) {
//       case (uid, pid, _, ct, card) =>
//         (Ref.of[IO, (Int, FiniteDuration)](0 -> 0.seconds), Ref.of[IO, Option[GivingUp]](None)).tupled.flatMap {
//           case (bgActions, retries) =>
//             implicit val bg = TestBackground.counter(bgActions)
//             implicit val rh = TestRetry.givingUp(retries)
//
//             Checkout[IO](successfulClient(pid), successfulCart(ct), failingOrders, retryPolicy)
//               .process(uid, card)
//               .attempt
//               .flatMap {
//                 case Left(OrderError(_)) =>
//                   (bgActions.get, retries.get).mapN {
//                     case (c, Some(g)) =>
//                       expect.same(c, 1 -> 1.hour) |+|
//                         expect.same(g.totalRetries, MaxRetries)
//                     case _ => failure(s"Expected $MaxRetries retries and reschedule")
//                   }
//                 case _ =>
//                   IO.pure(failure("Expected order error"))
//               }
//         }
//     }
//   }
//
//   test("failing to delete cart does not affect checkout") {
//     forall(gen) {
//       case (uid, pid, oid, ct, card) =>
//         Checkout[IO](
//           successfulClient(pid),
//           failingCart(ct),
//           successfulOrders(oid),
//           retryPolicy
//         ).process(uid, card)
//           .map(expect.same(oid, _))
//     }
//   }
//
//   test("successful checkout") {
//     forall(gen) {
//       case (uid, pid, oid, ct, card) =>
//         Checkout[IO](successfulClient(pid), successfulCart(ct), successfulOrders(oid), retryPolicy)
//           .process(uid, card)
//           .map(expect.same(oid, _))
//     }
//   }
//
// }
//
// protected class TestOrders() extends Orders[IO] {
//   def get(userId: UserId, orderId: OrderId): IO[Option[Order]]                                               = ???
//   def findBy(userId: UserId): IO[List[Order]]                                                                = ???
//   def create(userId: UserId, paymentId: PaymentId, items: NonEmptyList[CartItem], total: Money): IO[OrderId] = ???
// }
//
// protected class TestCart() extends ShoppingCart[IO] {
//   def add(userId: UserId, itemId: ItemId, quantity: Quantity): IO[Unit] = ???
//   def get(userId: UserId): IO[CartTotal]                                = ???
//   def delete(userId: UserId): IO[Unit]                                  = ???
//   def removeItem(userId: UserId, itemId: ItemId): IO[Unit]              = ???
//   def update(userId: UserId, cart: Cart): IO[Unit]                      = ???

protected class TestPosts() extends Posts[IO]:
  def create(text: String): IO[Post] = ???
  def get(id: Int, thread: Thread): IO[Option[Post]] = ???
  def getAll(thread: Thread): IO[List[Post]] = ???

protected class TestThreads() extends Threads[IO]:
  def add(post: Post, id: Int): IO[Unit] = ???
  def create(title: String, post: Post): IO[Int] = ???
  def get(id: Int): IO[Option[Thread]] = ???
  def getAll: IO[List[Thread]] = ???
