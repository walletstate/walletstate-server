package online.walletstate.services

import online.walletstate.domain.Namespace
import online.walletstate.repos.NamespaceRepo
import zio.{Task, ZIO, ZLayer}

import java.util.UUID

trait NamespaceService {
  def create(ns: Namespace): Task[Unit]
  def get(id: UUID): Task[Option[Namespace]]
}

object NamespaceService {
  def create(ns: Namespace): ZIO[NamespaceService, Throwable, Unit] =
    ZIO.serviceWithZIO[NamespaceService](_.create(ns))

  def get(id: UUID): ZIO[NamespaceService, Throwable, Option[Namespace]] =
    ZIO.serviceWithZIO[NamespaceService](_.get(id))
}

case class NamespaceServiceImpl(repo: NamespaceRepo) extends NamespaceService {
  override def create(ns: Namespace): Task[Unit] = repo.create(ns)

  override def get(id: UUID): Task[Option[Namespace]] = repo.get(id)
}

object NamespaceServiceImpl {
  val layer: ZLayer[NamespaceRepo, Nothing, NamespaceService] = ZLayer {
    for {
      namespaceRepo <- ZIO.service[NamespaceRepo]
    } yield NamespaceServiceImpl(namespaceRepo)
  }
}
