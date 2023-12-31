package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.Grouped
import online.walletstate.models.errors.AccountsGroupNotExist
import online.walletstate.models.{Group, Groupable, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZIO, ZLayer}

trait GroupsService {
  def create(wallet: Wallet.Id, `type`: Group.Type, name: String, orderingIndex: Int, user: User.Id): Task[Group]
  def update(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id, name: String, orderingIndex: Int): Task[Unit]
  def get(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id): Task[Group]
  def list(wallet: Wallet.Id, `type`: Group.Type): Task[Seq[Group]]
  def delete(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id): Task[Unit]
  def group[T <: Groupable](wallet: Wallet.Id, `type`: Group.Type, getItemsFn: Task[Seq[T]]): Task[Seq[Grouped[T]]]
}

final case class GroupsServiceLive(quill: WalletStateQuillContext) extends GroupsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(
      wallet: Wallet.Id,
      `type`: Group.Type,
      name: String,
      orderingIndex: Index,
      user: User.Id
  ): Task[Group] = for {
    group <- Group.make(wallet, `type`, name, orderingIndex, user)
    _     <- run(insert(group))
  } yield group

  override def update(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id, name: String, orderingIndex: Int): Task[Unit] =
    run(updateQuery(wallet, `type`, id, name, orderingIndex)).map(_ => ())

  override def get(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id): Task[Group] =
    run(groupsById(wallet, `type`, id)).map(_.headOption).getOrError(AccountsGroupNotExist)

  override def list(wallet: Wallet.Id, `type`: Group.Type): Task[Seq[Group]] =
    run(groupsByWallet(wallet, `type`))

  override def delete(wallet: Wallet.Id, `type`: Group.Type, id: Group.Id): Task[Unit] = for {
    _ <- run(deleteQuery(wallet, `type`, id)) // TODO: handle error and return CanNotDeleteAccountsGroup
  } yield ()

  override def group[T <: Groupable](
      wallet: Wallet.Id,
      `type`: Group.Type,
      getItemsFn: Task[Seq[T]]
  ): Task[Seq[Grouped[T]]] = for {
    groups  <- list(wallet, `type`)
    items   <- getItemsFn // TODO run in parallel
    grouped <- joinGroupsWithItems(groups, items)
  } yield grouped

  private def joinGroupsWithItems[T <: Groupable](groups: Seq[Group], items: Seq[T]) = ZIO.succeed {
    val itemsByGroup = items.groupBy(_.group)

    groups
      .sortBy(_.orderingIndex)
      .map { group =>
        Grouped(
          group.id,
          group.name,
          group.orderingIndex,
          itemsByGroup.getOrElse(group.id, Seq.empty[T]).sortBy(_.orderingIndex)
        )
      }
  }

  // queries
  private inline def insert(accountsGroup: Group) =
    quote(query[Group].insertValue(lift(accountsGroup)))
  private inline def groupsByWallet(wallet: Wallet.Id, `type`: Group.Type) =
    quote(query[Group].filter(_.wallet == lift(wallet))).filter(_.`type` == lift(`type`))
  private inline def groupsById(wallet: Wallet.Id, `type`: Group.Type, group: Group.Id) =
    groupsByWallet(wallet, `type`).filter(_.id == lift(group))
  private inline def updateQuery(wallet: Wallet.Id, `type`: Group.Type, group: Group.Id, name: String, orderingIndex: Int) =
    groupsById(wallet, `type`, group).update(_.name -> lift(name), _.orderingIndex -> lift(orderingIndex))
  private inline def deleteQuery(wallet: Wallet.Id, `type`: Group.Type, group: Group.Id) =
    groupsById(wallet, `type`, group).delete
}

object GroupsServiceLive {
  val layer = ZLayer.fromFunction(GroupsServiceLive.apply _)
}
