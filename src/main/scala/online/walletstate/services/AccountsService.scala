package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models
import online.walletstate.models.api.{CreateAccount, Grouped}
import online.walletstate.models.{Account, AppError, Group, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait AccountsService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAccount): Task[Account]
  def get(wallet: Wallet.Id, id: Account.Id): Task[Account]
  def list(wallet: Wallet.Id): Task[List[Account]]
  def list(wallet: Wallet.Id, group: Group.Id): Task[List[Account]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Account]]]
}

final case class AccountsServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends AccountsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAccount): Task[Account] = for {
    account <- Account.make(info) // TODO check group has correct type and group is in current wallet
    _       <- run(insert(account))
  } yield account

  override def get(wallet: Wallet.Id, id: Account.Id): Task[Account] =
    run(accountsById(wallet, id)).map(_.headOption).getOrError(AppError.AccountNotExist())

  override def list(wallet: Wallet.Id): Task[List[Account]] =
    run(accountsByWallet(wallet))

  override def list(wallet: Wallet.Id, group: Group.Id): Task[List[Account]] =
    run(accountsByGroup(wallet, group))

  def grouped(wallet: Wallet.Id): Task[List[Grouped[Account]]] =
    groupsService.group(wallet, Group.Type.Accounts, list(wallet))

  // queries
  private inline def insert(account: Account) = quote(query[Account].insertValue(lift(account)))

  private inline def accountsByWallet(wallet: Wallet.Id) = quote {
    query[Account]
      .join(query[Group])
      .on(_.group == _.id)
      .filter { case (_, group) => group.wallet == lift(wallet) }
      .map { case (account, _) => account }
  }

  private inline def accountsByGroup(wallet: Wallet.Id, group: Group.Id) =
    accountsByWallet(wallet).filter(_.group == lift(group))

  private inline def accountsById(wallet: Wallet.Id, id: Account.Id) =
    accountsByWallet(wallet).filter(_.id == lift(id))
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
