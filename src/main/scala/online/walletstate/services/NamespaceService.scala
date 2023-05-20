package online.walletstate.services

import online.walletstate.domain.Namespace
import online.walletstate.repos.NamespaceRepo
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

trait NamespaceService {
  def create(ns: Namespace): Task[UUID]
  def get(id: UUID): Task[Option[Namespace]]
}

case class NamespaceServiceImpl(repo: NamespaceRepo) extends NamespaceService {
  override def create(ns: Namespace): Task[UUID] = repo.create(ns)

  override def get(id: UUID): Task[Option[Namespace]] = repo.get(id)
}

object NamespaceServiceImpl {
  val layer: ZLayer[NamespaceRepo, Nothing, NamespaceService] = ZLayer {
    for {
      namespaceRepo <- ZIO.service[NamespaceRepo]
    } yield NamespaceServiceImpl(namespaceRepo)
  }
}
