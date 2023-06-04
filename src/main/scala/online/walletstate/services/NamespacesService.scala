package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.*
import online.walletstate.models.{Namespace, NamespaceInvite, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Clock, Task, ZIO, ZLayer}

import scala.util.Random

trait NamespacesService {
  def create(userId: User.Id, name: String): Task[Namespace]
  def get(id: Namespace.Id): Task[Namespace]
  def createInvite(userId: User.Id, namespace: Namespace.Id): Task[NamespaceInvite]
  def joinNamespace(userId: User.Id, inviteCode: String): Task[Namespace]
}

case class NamespacesServiceLive(
    quill: QuillCtx,
    usersService: UsersService,
    invitesService: NamespaceInvitesService
) extends NamespacesService {

  import io.getquill.*
  import quill.*

  override def create(userId: User.Id, name: String): Task[Namespace] = for {
    user      <- usersService.get(userId)
    _         <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    namespace <- Namespace.make(name, user.id)
    _         <- run(insert(namespace))
    _         <- usersService.setNamespace(user.id, namespace.id)
  } yield namespace

  override def get(id: Namespace.Id): Task[Namespace] =
    run(namespaceById(id)).map(_.headOption).getOrError(NamespaceNotExist)

  // TODO make expiration configurable and move to invites service
  override def createInvite(userId: User.Id, namespace: Namespace.Id): Task[NamespaceInvite] = for {
    now    <- Clock.instant
    code   <- ZIO.attempt(Random.alphanumeric.take(12).mkString.toUpperCase)
    invite <- NamespaceInvite.make(namespace, code, userId, now.plusSeconds(3600))
    _      <- invitesService.save(invite)
  } yield invite

  override def joinNamespace(userId: User.Id, inviteCode: String): Task[Namespace] = for {
    user      <- usersService.get(userId)
    _         <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    invite    <- invitesService.get(inviteCode)
    now       <- Clock.instant
    _         <- if (invite.validTo.isBefore(now)) ZIO.fail(NamespaceInviteExpired) else ZIO.unit
    namespace <- get(invite.namespace)
    _         <- usersService.setNamespace(userId, invite.namespace)
    _         <- invitesService.delete(invite.id)
  } yield namespace

  // queries
  private inline def insert(ns: Namespace)           = quote(query[Namespace].insertValue(lift(ns)))
  private inline def namespaceById(id: Namespace.Id) = quote(query[Namespace].filter(_.id == lift(id)))

}

object NamespacesServiceLive {
  val layer =
    ZLayer.fromFunction(NamespacesServiceLive.apply _)

}
