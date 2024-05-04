package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateAccount, Grouped, UpdateAccount}
import online.walletstate.models.*
import online.walletstate.services.queries.AccountsQuillQueries
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait AccountsService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAccount): Task[Account]
  def get(wallet: Wallet.Id, id: Account.Id): Task[Account]
  def list(wallet: Wallet.Id): Task[List[Account]]
  def list(wallet: Wallet.Id, group: Group.Id): Task[List[Account]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Account]]]
  def update(wallet: Wallet.Id, id: Account.Id, info: UpdateAccount): Task[Unit]
}

final case class AccountsServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends AccountsService
    with AccountsQuillQueries {
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

  override def update(wallet: Wallet.Id, id: Account.Id, info: UpdateAccount): Task[Unit] = for {
    // TODO check account is in wallet. check update result
    _ <- run(update(id, info))
  } yield ()
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
