package online.walletstate.repos

import online.walletstate.domain.namespaces.Namespace
import online.walletstate.domain.namespaces.errors.NamespaceNotExist
import zio.{Task, ULayer, ZIO, ZLayer}

import java.util.UUID
import scala.collection.concurrent.TrieMap

trait NamespacesRepo {
  def save(ns: Namespace): Task[Namespace]
  def get(id: UUID): Task[Namespace]
}

case class NamespacesRepoLive(quill: QuillCtx) extends NamespacesRepo {
  import io.getquill.*
  import quill.*

  override def save(ns: Namespace): Task[Namespace] =
    run(quote(query[Namespace].insertValue(lift(ns)))).map(_ => ns)

  override def get(id: UUID): Task[Namespace] =
    run(quote(query[Namespace].filter(_.id == lift(id)))).map(_.headOption).flatMap {
      case Some(value) => ZIO.succeed(value)
      case None        => ZIO.fail(NamespaceNotExist)
    }
}

object NamespacesRepoLive {
  val layer: ZLayer[QuillCtx, Nothing, NamespacesRepo] =
    ZLayer.fromFunction(NamespacesRepoLive.apply _)
}
