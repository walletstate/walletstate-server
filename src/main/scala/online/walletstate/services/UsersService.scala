package online.walletstate.services

import online.walletstate.models.users.User
import online.walletstate.models.users.errors.UserNotExist
import online.walletstate.db.QuillCtx
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.*

import java.util.UUID

trait UsersService {
  def create(user: User): Task[User]
  def get(userId: String): Task[User]
  def setNamespace(userId: String, namespace: UUID): Task[Unit]
}

final case class UsersServiceLive(quill: QuillCtx) extends UsersService {
  import quill.*
  import io.getquill.*

  override def create(user: User): Task[User] =
    run(insertUser(user)).map(_ => user)

  override def get(userId: String): Task[User] =
    run(userById(userId)).map(_.headOption).getOrError(UserNotExist)

  override def setNamespace(userId: String, namespace: UUID): Task[Unit] =
    run(updateNamespace(userId, namespace)).map(_ => ())

  // queries
  private inline def userById(userId: String) = quote(query[User].filter(_.id == lift(userId)))
  private inline def insertUser(user: User)   = quote(query[User].insertValue(lift(user)))
  private inline def updateNamespace(userId: String, namespace: UUID) =
    quote(userById(userId).update(_.namespace -> Some(lift(namespace))))
}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
