package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.AccountsGroupNotExist
import online.walletstate.models.{AccountsGroup, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait AccountsGroupsService {
  def create(wallet: Wallet.Id, name: String, orderingIndex: Int, user: User.Id): Task[AccountsGroup]
  def update(wallet: Wallet.Id, id: AccountsGroup.Id, name: String): Task[Unit]
  def get(wallet: Wallet.Id, id: AccountsGroup.Id): Task[AccountsGroup]
  def list(wallet: Wallet.Id): Task[Seq[AccountsGroup]]
}

final case class AccountsGroupsServiceLive(quill: QuillCtx) extends AccountsGroupsService {
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

  // queries
  private inline def insert(accountsGroup: AccountsGroup) = quote(query[AccountsGroup].insertValue(lift(accountsGroup)))
  private inline def groupsByWallet(wallet: Wallet.Id)    = quote(query[AccountsGroup].filter(_.wallet == lift(wallet)))
  private inline def groupsById(wallet: Wallet.Id, group: AccountsGroup.Id) =
    groupsByWallet(wallet).filter(_.id == lift(group))
  private inline def updateQuery(wallet: Wallet.Id, group: AccountsGroup.Id, name: String) =
    groupsById(wallet, group).update(_.name -> lift(name))
}

object AccountsGroupsServiceLive {
  val layer = ZLayer.fromFunction(AccountsGroupsServiceLive.apply _)
}
