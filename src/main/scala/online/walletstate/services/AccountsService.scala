package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.AccountNotExist
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.*
import online.walletstate.services.queries.AccountsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait AccountsService {
  def create(data: Account.Data): WalletUIO[Account]
  def get(id: Account.Id): WalletIO[AccountNotExist, Account]
  def list: WalletUIO[List[Account]]
  def list(group: Group.Id): WalletUIO[List[Account]]
  def grouped: WalletUIO[List[Grouped[Account]]]
  def update(id: Account.Id, data: Account.Data): WalletUIO[Unit]
}

final case class AccountsServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends AccountsService
    with AccountsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(data: Account.Data): WalletUIO[Account] = for {
    account <- Account.make(data) // TODO check group has correct type and group is in current wallet
    _       <- run(insert(account)).orDie
  } yield account

  override def get(id: Account.Id): WalletIO[AccountNotExist, Account] = for {
    ctx     <- ZIO.service[WalletContext]
    account <- run(accountsById(ctx.wallet, id)).orDie.headOrError(AccountNotExist())
  } yield account

  override def list: WalletUIO[List[Account]] = for {
    ctx      <- ZIO.service[WalletContext]
    accounts <- run(accountsByWallet(ctx.wallet)).orDie
  } yield accounts

  override def list(group: Group.Id): WalletUIO[List[Account]] = for {
    ctx      <- ZIO.service[WalletContext]
    accounts <- run(accountsByGroup(ctx.wallet, group)).orDie
  } yield accounts

  def grouped: WalletUIO[List[Grouped[Account]]] = for {
//    ctx     <- ZIO.service[WalletContext]
    grouped <- groupsService.group(Group.Type.Accounts, list)
  } yield grouped

  override def update(id: Account.Id,  data: Account.Data): WalletUIO[Unit] = for {
    // TODO check account is in wallet. check update result
    _ <- run(updateQuery(id, data)).orDie
  } yield ()
}

object AccountsServiceLive {
  val layer = ZLayer.fromFunction(AccountsServiceLive.apply _)
}
