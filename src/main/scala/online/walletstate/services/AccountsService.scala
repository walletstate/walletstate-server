package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models
import online.walletstate.models.errors.AccountNotExist
import online.walletstate.models.{Account, AccountsGroup, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait AccountsService {
  def create(group: AccountsGroup.Id, name: String, orderingIndex: Int, icon: String, user: User.Id): Task[Account]
  def get(wallet: Wallet.Id, id: Account.Id): Task[Account]
  def list(wallet: Wallet.Id): Task[Seq[Account]]
}

final case class AccountsServiceLive(quill: QuillCtx) extends AccountsService {
  import io.getquill.*
  import quill.*

  override def create(
      group: AccountsGroup.Id,
      name: String,
      orderingIndex: Int,
      icon: String,
      user: User.Id
  ): Task[Account] = for {
    account <- Account.make(group, name, orderingIndex, icon, user)
    _       <- run(insert(account))
  } yield account

  override def get(wallet: Wallet.Id, id: Account.Id): Task[Account] =
    run(accountsById(wallet, id)).map(_.headOption).getOrError(AccountNotExist)

  override def list(wallet: Wallet.Id): Task[Seq[Account]] =
    run(accountsByWallet(wallet))

  // queries
  private inline def insert(account: Account) = quote(query[Account].insertValue(lift(account)))

  private inline def accountsByWallet(wallet: Wallet.Id) = quote {
    query[Account]
      .join(query[AccountsGroup])
      .on(_.group == _.id)
      .filter { case (_, group) => group.wallet == lift(wallet) }
      .map { case (account, _) => account }
  }

  private inline def accountsById(wallet: Wallet.Id, id: Account.Id) = accountsByWallet(wallet).filter(_.id == lift(id))
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
