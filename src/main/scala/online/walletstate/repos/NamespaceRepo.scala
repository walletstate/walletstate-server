package online.walletstate.repos

import online.walletstate.domain.Namespace
import zio.{Task, ULayer, ZIO, ZLayer}

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait NamespaceRepo {
  def create(ns: Namespace): Task[Unit]
  def get(id: UUID): Task[Option[Namespace]]
}
//
//object NamespaceRepo {
//  def create(ns: Namespace): ZIO[NamespaceRepo, Throwable, Unit] =
//    ZIO.serviceWithZIO[NamespaceRepo](_.create(ns))
//
//  def get(id: UUID): ZIO[NamespaceRepo, Throwable, Option[Namespace]] =
//    ZIO.serviceWithZIO[NamespaceRepo](_.get(id))
//}

case class ImMemoryNamespaceRepo() extends NamespaceRepo {
  private val storage = TrieMap.empty[UUID, Namespace]

  override def create(ns: Namespace): Task[Unit] =
    ZIO.succeed {
      storage.put(ns.id, ns)
      ()
    }

  override def get(id: UUID): Task[Option[Namespace]] =
    ZIO.succeed(storage.get(id))
}

object ImMemoryNamespaceRepo {
  val layer: ULayer[NamespaceRepo] = ZLayer.succeed(ImMemoryNamespaceRepo())
}
