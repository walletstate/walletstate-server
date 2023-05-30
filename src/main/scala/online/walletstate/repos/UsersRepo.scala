package online.walletstate.repos

import online.walletstate.domain.users.User
import online.walletstate.domain.users.errors.UserNotExist
import zio.*

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait UsersRepo {
  def save(user: User): Task[User]
  def get(userId: String): Task[User]
  def setNamespace(userId: String, namespace: UUID): Task[Unit]
}

case class UsersRepoLive(quill: QuillCtx) extends UsersRepo {
  import io.getquill.*
  import quill.*

  override def save(user: User): Task[User] =
    run(quote(query[User].insertValue(lift(user)))).map(_ => user)

  override def get(userId: String): Task[User] =
    run(quote(query[User].filter(_.id == lift(userId)))).map(_.headOption).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.fail(UserNotExist)
    }

  override def setNamespace(userId: String, namespace: UUID): Task[Unit] = run {
    quote(query[User].filter(_.id == lift(userId)).update(_.namespace -> Some(lift(namespace))))
  }.map(_ => ())
}

object UsersRepoLive {
  val layer: ZLayer[QuillCtx, Nothing, UsersRepo] = ZLayer.fromFunction(UsersRepoLive.apply _)
}
