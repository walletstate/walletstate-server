package online.walletstate.repos

import online.walletstate.domain.Namespace
import zio.{Task, ULayer, ZIO, ZLayer}

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait NamespaceRepo {
  def create(ns: Namespace): Task[UUID]
  def get(id: UUID): Task[Option[Namespace]]
}


case class InMemoryNamespaceRepo() extends NamespaceRepo {
  private val storage = TrieMap.empty[UUID, Namespace]

  override def create(ns: Namespace): Task[UUID] =
    ZIO.succeed {
      storage.put(ns.id, ns)
      ns.id
    }

  override def get(id: UUID): Task[Option[Namespace]] =
    ZIO.succeed(storage.get(id))
}

object InMemoryNamespaceRepo {
  val layer: ULayer[NamespaceRepo] = ZLayer.succeed(InMemoryNamespaceRepo())
}
