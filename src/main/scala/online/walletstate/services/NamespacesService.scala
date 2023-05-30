package online.walletstate.services

import online.walletstate.domain.namespaces.errors.*
import online.walletstate.domain.namespaces.{CreateNamespace, Namespace, NamespaceInvite}
import online.walletstate.repos.{NamespacesRepo, UsersRepo, NamespaceInvitesRepo}
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
    namespacesRepo: NamespacesRepo,
    usersRepo: UsersRepo,
    invitesRepo: NamespaceInvitesRepo
) extends NamespacesService {
  override def create(userId: String, ns: CreateNamespace): Task[Namespace] = for {
    user      <- usersRepo.get(userId)
    _         <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    namespace <- namespacesRepo.save(Namespace(ns.name, user.id))
    _         <- usersRepo.setNamespace(user.id, namespace.id)
  } yield namespace

  override def get(id: UUID): Task[Namespace] = namespacesRepo.get(id)

  // TODO make expiration configurable
  override def createInvite(userId: String, namespace: UUID): Task[NamespaceInvite] = for {
    now <- Clock.instant
    code = Random.alphanumeric.take(12).mkString.toUpperCase
    invite <- invitesRepo.save(NamespaceInvite(namespace, code, userId, now.plusSeconds(3600)))
  } yield invite

  override def joinNamespace(userId: String, inviteCode: String): Task[Namespace] = for {
    user      <- usersRepo.get(userId)
    _         <- if (user.namespace.nonEmpty) ZIO.fail(UserAlreadyHasNamespace) else ZIO.unit
    invite    <- invitesRepo.get(inviteCode)
    now       <- Clock.instant
    _         <- if (invite.validTo.isBefore(now)) ZIO.fail(NamespaceInviteExpired) else ZIO.unit
    namespace <- namespacesRepo.get(invite.namespaceId)
    _         <- usersRepo.setNamespace(userId, invite.namespaceId)
    _         <- invitesRepo.delete(invite.id)
  } yield namespace
}

object NamespacesServiceLive {
  val layer: ZLayer[NamespacesRepo with UsersRepo with NamespaceInvitesRepo, Nothing, NamespacesService] =
    ZLayer.fromFunction(NamespacesServiceLive.apply _)

}
