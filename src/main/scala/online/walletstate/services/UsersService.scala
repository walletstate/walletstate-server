package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.UserNotExist
import online.walletstate.models.{Namespace, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.*

trait UsersService {
  def create(user: User): Task[User]
  def get(id: User.Id): Task[User]
  def setNamespace(user: User.Id, namespace: Namespace.Id): Task[Unit]
}

final case class UsersServiceLive(quill: QuillCtx) extends UsersService {
  import io.getquill.*
  import quill.*

  override def create(user: User): Task[User] =
    run(insertUser(user)).map(_ => user)

  override def get(id: User.Id): Task[User] =
    run(userById(id)).map(_.headOption).getOrError(UserNotExist)

  override def setNamespace(user: User.Id, namespace: Namespace.Id): Task[Unit] =
    run(updateNamespace(user, namespace)).map(_ => ())

  // queries
  private inline def userById(id: User.Id)  = quote(query[User].filter(_.id == lift(id)))
  private inline def insertUser(user: User) = quote(query[User].insertValue(lift(user)))
  private inline def updateNamespace(user: User.Id, namespace: Namespace.Id) =
    quote(userById(user).update(_.namespace -> Some(lift(namespace))))
}

object UsersServiceLive {
  val layer = ZLayer.fromFunction(UsersServiceLive.apply _)
}
