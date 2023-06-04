package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.AccountNotExist
import online.walletstate.models.{Account, Namespace, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Task, ZLayer}

trait AccountsService {
  def create(namespace: Namespace.Id, name: String, user: User.Id): Task[Account]
  def get(namespace: Namespace.Id, id: Account.Id): Task[Account]
  def list(namespace: Namespace.Id): Task[Seq[Account]]
}

final case class AccountsServiceLive(quill: QuillCtx) extends AccountsService {
  import io.getquill.*
  import quill.*

  override def create(namespace: Namespace.Id, name: String, user: User.Id): Task[Account] = for {
    account <- Account.make(namespace, name, user)
    _       <- run(insert(account))
  } yield account

  override def get(namespace: Namespace.Id, id: Account.Id): Task[Account] =
    run(accountsById(namespace, id)).map(_.headOption).getOrError(AccountNotExist)

  override def list(namespace: Namespace.Id): Task[Seq[Account]] =
    run(accountsByNs(namespace))

  // queries
  private inline def insert(account: Account)       = quote(query[Account].insertValue(lift(account)))
  private inline def accountsByNs(ns: Namespace.Id) = quote(query[Account].filter(_.namespace == lift(ns)))
  private inline def accountsById(ns: Namespace.Id, id: Account.Id) = accountsByNs(ns).filter(_.id == lift(id))
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
