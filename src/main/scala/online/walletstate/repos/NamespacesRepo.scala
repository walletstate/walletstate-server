package online.walletstate.repos

import online.walletstate.domain.namespaces.Namespace
import zio.{Task, ULayer, ZIO, ZLayer}

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait NamespaceRepo {
  def create(ns: Namespace): Task[Namespace]
  def get(id: UUID): Task[Option[Namespace]]
}

case class InMemoryNamespacesRepo() extends NamespaceRepo {
  private val storage = TrieMap.empty[UUID, Namespace]

  override def create(ns: Namespace): Task[Namespace] =
    ZIO.succeed {
      storage.put(ns.id, ns)
      ns
    }

  override def get(id: UUID): Task[Option[Namespace]] =
    ZIO.succeed(storage.get(id))
}

object InMemoryNamespacesRepo {
  val layer: ULayer[NamespaceRepo] = ZLayer.succeed(InMemoryNamespacesRepo())
}
