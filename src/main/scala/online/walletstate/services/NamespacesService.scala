package online.walletstate.services

import online.walletstate.models.namespaces.errors.*
import online.walletstate.models.namespaces.{CreateNamespace, Namespace, NamespaceInvite}
import online.walletstate.models.db.QuillCtx
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Clock, Task, ZIO, ZLayer}

import java.util.UUID
import scala.util.Random

trait NamespacesService {
  def create(userId: String, ns: CreateNamespace): Task[Namespace]
  def get(id: UUID): Task[Namespace]
  def createInvite(userId: String, namespace: UUID): Task[NamespaceInvite]
  def joinNamespace(userId: String, inviteCode: String): Task[Namespace]
}

case class NamespacesServiceLive(
    quill: QuillCtx,
    usersService: UsersService,
    invitesService: NamespaceInvitesService
) extends NamespacesService {

  import quill.*
  import io.getquill.*

  override def create(userId: String, ns: CreateNamespace): Task[Namespace] = for {
    user <- usersService.get(userId)
    _    <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    namespace = Namespace(ns.name, user.id)
    _ <- run(insert(namespace))
    _ <- usersService.setNamespace(user.id, namespace.id)
  } yield namespace

  override def get(id: UUID): Task[Namespace] =
    run(namespaceById(id)).map(_.headOption).getOrError(NamespaceNotExist)

  // TODO make expiration configurable
  override def createInvite(userId: String, namespace: UUID): Task[NamespaceInvite] = for {
    now <- Clock.instant
    code = Random.alphanumeric.take(12).mkString.toUpperCase
    invite <- invitesService.save(NamespaceInvite(namespace, code, userId, now.plusSeconds(3600)))
  } yield invite

  override def joinNamespace(userId: String, inviteCode: String): Task[Namespace] = for {
    user      <- usersService.get(userId)
    _         <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    invite    <- invitesService.get(inviteCode)
    now       <- Clock.instant
    _         <- if (invite.validTo.isBefore(now)) ZIO.fail(NamespaceInviteExpired) else ZIO.unit
    namespace <- get(invite.namespaceId)
    _         <- usersService.setNamespace(userId, invite.namespaceId)
    _         <- invitesService.delete(invite.id)
  } yield namespace

  // queries
  private inline def insert(ns: Namespace)   = quote(query[Namespace].insertValue(lift(ns)))
  private inline def namespaceById(id: UUID) = quote(query[Namespace].filter(_.id == lift(id)))

}

object NamespacesServiceLive {
  val layer =
    ZLayer.fromFunction(NamespacesServiceLive.apply _)

}
