package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.AccountNotExist
import online.walletstate.models.{Account, Wallet, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Task, ZLayer}

trait AccountsService {
  def create(wallet: Wallet.Id, name: String, user: User.Id): Task[Account]
  def get(wallet: Wallet.Id, id: Account.Id): Task[Account]
  def list(wallet: Wallet.Id): Task[Seq[Account]]
}

final case class AccountsServiceLive(quill: QuillCtx) extends AccountsService {
  import io.getquill.*
  import quill.*

  override def create(wallet: Wallet.Id, name: String, user: User.Id): Task[Account] = for {
    account <- Account.make(wallet, name, user)
    _       <- run(insert(account))
  } yield account

  override def get(wallet: Wallet.Id, id: Account.Id): Task[Account] =
    run(accountsById(wallet, id)).map(_.headOption).getOrError(AccountNotExist)

  override def list(wallet: Wallet.Id): Task[Seq[Account]] =
    run(accountsByNs(wallet))

  // queries
  private inline def insert(account: Account)       = quote(query[Account].insertValue(lift(account)))
  private inline def accountsByNs(ns: Wallet.Id) = quote(query[Account].filter(_.wallet == lift(ns)))
  private inline def accountsById(ns: Wallet.Id, id: Account.Id) = accountsByNs(ns).filter(_.id == lift(id))
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
