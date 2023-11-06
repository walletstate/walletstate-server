package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.api.{AccountsGroupWithAccounts, SimpleAccount}
import online.walletstate.models.errors.{AccountsGroupNotExist, CanNotDeleteAccountsGroup}
import online.walletstate.models.{Account, AccountsGroup, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZIO, ZLayer}

trait AccountsGroupsService {
  def create(wallet: Wallet.Id, name: String, orderingIndex: Int, user: User.Id): Task[AccountsGroup]
  def update(wallet: Wallet.Id, id: AccountsGroup.Id, name: String): Task[Unit]
  def get(wallet: Wallet.Id, id: AccountsGroup.Id): Task[AccountsGroup]
  def list(wallet: Wallet.Id): Task[Seq[AccountsGroup]]
  def delete(wallet: Wallet.Id, id: AccountsGroup.Id): Task[Unit]
  def listWithAccounts(wallet: Wallet.Id): Task[Seq[AccountsGroupWithAccounts]]
}

final case class AccountsGroupsServiceLive(quill: QuillCtx, accountsService: AccountsService)
    extends AccountsGroupsService {
  import io.getquill.*
  import quill.*

  override def create(wallet: Wallet.Id, name: String, orderingIndex: Index, user: User.Id): Task[AccountsGroup] = for {
    group <- AccountsGroup.make(wallet, name, orderingIndex, user)
    _     <- run(insert(group))
  } yield group

  override def update(wallet: Wallet.Id, id: AccountsGroup.Id, name: String): Task[Unit] =
    run(updateQuery(wallet, id, name)).map(_ => ())

  override def get(wallet: Wallet.Id, id: AccountsGroup.Id): Task[AccountsGroup] =
    run(groupsById(wallet, id)).map(_.headOption).getOrError(AccountsGroupNotExist)

  override def list(wallet: Wallet.Id): Task[Seq[AccountsGroup]] =
    run(groupsByWallet(wallet))
  
  override def delete(wallet: Wallet.Id, id: AccountsGroup.Id): Task[Unit] = for {
    accounts <- accountsService.list(wallet, id) //TODO Optimize to not load all accounts just for count
    _ <- if(accounts.nonEmpty) ZIO.fail(CanNotDeleteAccountsGroup) else run(deleteQuery(wallet, id))
  } yield ()

  override def listWithAccounts(wallet: Wallet.Id): Task[Seq[AccountsGroupWithAccounts]] = for {
    groups             <- list(wallet)
    accounts           <- accountsService.list(wallet) // TODO run in parallel
    groupsWithAccounts <- joinGroupsWithAccounts(groups, accounts)
  } yield groupsWithAccounts

  private def joinGroupsWithAccounts(groups: Seq[AccountsGroup], accounts: Seq[Account]) = ZIO.succeed {
    val accountsByGroupId = accounts.groupBy(_.group)

    groups
      .sortBy(_.orderingIndex)
      .map { group =>
        AccountsGroupWithAccounts
          .build(
            group,
            accountsByGroupId
              .getOrElse(group.id, Seq.empty[Account])
              .map(SimpleAccount.fromAccount)
              .sortBy(_.orderingIndex)
          )
      }
  }

  // queries
  private inline def insert(accountsGroup: AccountsGroup) = quote(query[AccountsGroup].insertValue(lift(accountsGroup)))
  private inline def groupsByWallet(wallet: Wallet.Id)    = quote(query[AccountsGroup].filter(_.wallet == lift(wallet)))
  private inline def groupsById(wallet: Wallet.Id, group: AccountsGroup.Id) =
    groupsByWallet(wallet).filter(_.id == lift(group))
  private inline def updateQuery(wallet: Wallet.Id, group: AccountsGroup.Id, name: String) =
    groupsById(wallet, group).update(_.name -> lift(name))
  private inline def deleteQuery(wallet: Wallet.Id, group: AccountsGroup.Id) = groupsById(wallet, group).delete
}

object AccountsGroupsServiceLive {
  val layer = ZLayer.fromFunction(AccountsGroupsServiceLive.apply _)
}
