package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.Grouped
import online.walletstate.models.{AppError, Group, Groupable, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZIO, ZLayer}

trait GroupsService {
  def create(wallet: Wallet.Id, `type`: Group.Type, name: String, idx: Int, user: User.Id): Task[Group]
  def update(wallet: Wallet.Id, id: Group.Id, name: String, idx: Int): Task[Unit]
  def get(wallet: Wallet.Id, id: Group.Id): Task[Group]
  def list(wallet: Wallet.Id, `type`: Group.Type): Task[List[Group]]
  def delete(wallet: Wallet.Id, id: Group.Id): Task[Unit]
  def group[T <: Groupable](wallet: Wallet.Id, `type`: Group.Type, getItemsFn: Task[List[T]]): Task[List[Grouped[T]]]
}

final case class GroupsServiceLive(quill: WalletStateQuillContext) extends GroupsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(
      wallet: Wallet.Id,
      `type`: Group.Type,
      name: String,
      idx: Index,
      user: User.Id
  ): Task[Group] = for {
    group <- Group.make(wallet, `type`, name, idx)
    _     <- run(insert(group))
  } yield group

  override def update(
      wallet: Wallet.Id,
      id: Group.Id,
      name: String,
      idx: Int
  ): Task[Unit] =
    run(updateQuery(wallet, id, name, idx)).map(_ => ())

  override def get(wallet: Wallet.Id, id: Group.Id): Task[Group] =
    run(groupsById(wallet, id)).map(_.headOption).getOrError(AppError.AccountsGroupNotExist)

  override def list(wallet: Wallet.Id, `type`: Group.Type): Task[List[Group]] =
    run(groupsByType(wallet, `type`))

  override def delete(wallet: Wallet.Id, id: Group.Id): Task[Unit] = for {
    _ <- run(deleteQuery(wallet, id)) // TODO: handle error and return CanNotDeleteAccountsGroup
  } yield ()

  override def group[T <: Groupable](
      wallet: Wallet.Id,
      `type`: Group.Type,
      getItemsFn: Task[List[T]]
  ): Task[List[Grouped[T]]] = for {
    groups  <- list(wallet, `type`)
    items   <- getItemsFn // TODO run in parallel
    grouped <- joinGroupsWithItems(groups, items)
  } yield grouped

  private def joinGroupsWithItems[T <: Groupable](groups: List[Group], items: List[T]) = ZIO.succeed {
    val itemsByGroup = items.groupBy(_.group)

    groups
      .sortBy(_.idx)
      .map { group =>
        Grouped(
          group.id,
          group.name,
          group.idx,
          itemsByGroup.getOrElse(group.id, List.empty[T]).sortBy(_.idx)
        )
      }
  }

  // queries
  private inline def insert(accountsGroup: Group) = quote(query[Group].insertValue(lift(accountsGroup)))
    
  private inline def groupsByWallet(wallet: Wallet.Id) = quote(query[Group].filter(_.wallet == lift(wallet)))

  private inline def groupsByType(wallet: Wallet.Id, `type`: Group.Type) =
    quote(groupsByWallet(wallet).filter(_.`type` == lift(`type`)))

  private inline def groupsById(wallet: Wallet.Id, group: Group.Id) =
    groupsByWallet(wallet).filter(_.id == lift(group))

  private inline def updateQuery(
      wallet: Wallet.Id,
      group: Group.Id,
      name: String,
      idx: Int
  ) = groupsById(wallet, group).update(_.name -> lift(name), _.idx -> lift(idx))

  private inline def deleteQuery(wallet: Wallet.Id, group: Group.Id) = groupsById(wallet, group).delete
}

object GroupsServiceLive {
  val layer = ZLayer.fromFunction(GroupsServiceLive.apply _)
}
