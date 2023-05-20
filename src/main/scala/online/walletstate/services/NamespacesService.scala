package online.walletstate.services

import online.walletstate.domain.namespaces.Namespace
import online.walletstate.repos.NamespaceRepo
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

trait NamespacesService {
  def create(ns: Namespace): Task[Namespace]
  def get(id: UUID): Task[Option[Namespace]]
}

case class NamespacesServiceImpl(repo: NamespaceRepo) extends NamespacesService {
  override def create(ns: Namespace): Task[Namespace] = repo.create(ns)

  override def get(id: UUID): Task[Option[Namespace]] = repo.get(id)
}

object NamespacesServiceImpl {
  val layer: ZLayer[NamespaceRepo, Nothing, NamespacesService] = ZLayer {
    for {
      namespaceRepo <- ZIO.service[NamespaceRepo]
    } yield NamespacesServiceImpl(namespaceRepo)
  }
}
