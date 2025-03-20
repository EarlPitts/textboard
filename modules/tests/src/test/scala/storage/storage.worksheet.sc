import cats.data.EitherT
import cats.data.OptionT
import cats.data.Kleisli
import cats.effect._, org.http4s._, org.http4s.dsl.io._
import cats._
import cats.implicits._
import cats.effect.unsafe.IORuntime
import doobie.*
import doobie.implicits.*
import cats.effect.unsafe.implicits.global
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import cats.arrow.FunctionK
import cats.data.State
import scala.util.Try

implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]

val xa = Transactor.fromDriverManager[IO](
  driver = "org.sqlite.JDBC",
  url = "jdbc:sqlite:textboard.db",
  user = "",
  password = "",
  logHandler = None
)


List(sql"post", sql"thread")
  .map(table => sql"DELETE FROM ${table}")
