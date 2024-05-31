package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.*
import online.walletstate.models.AppError.GroupNotExist
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.api.Grouped
import online.walletstate.services.queries.GroupsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait GroupsService {
  def create(`type`: Group.Type, name: String, idx: Int): WalletUIO[Group]
  def update(id: Group.Id, name: String, idx: Int): WalletUIO[Unit]
  def get(id: Group.Id): WalletIO[GroupNotExist, Group]
  def list(`type`: Group.Type): WalletUIO[List[Group]]
  def delete(id: Group.Id): WalletUIO[Unit]
  def group[T <: Groupable](`type`: Group.Type, getItemsFn: WalletUIO[List[T]]): WalletUIO[List[Grouped[T]]]
}

final case class GroupsServiceLive(quill: WalletStateQuillContext) extends GroupsService with GroupsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(`type`: Group.Type, name: String, idx: Index): WalletUIO[Group] = for {
    ctx   <- ZIO.service[WalletContext]
    group <- Group.make(ctx.wallet, `type`, name, idx)
    _     <- run(insert(group)).orDie
  } yield group

  override def update(id: Group.Id, name: String, idx: Int): WalletUIO[Unit] =
    ZIO.serviceWithZIO[WalletContext] { ctx =>
      run(updateQuery(ctx.wallet, id, name, idx)).orDie.map(_ => ())
    }

  override def get(id: Group.Id): WalletIO[GroupNotExist, Group] = ZIO.serviceWithZIO[WalletContext] { ctx =>
    run(groupsById(ctx.wallet, id)).orDie.headOrError(GroupNotExist())
  }

  override def list(`type`: Group.Type): WalletUIO[List[Group]] = ZIO.serviceWithZIO[WalletContext] { ctx =>
    run(groupsByType(ctx.wallet, `type`)).orDie
  }

  override def delete(id: Group.Id): WalletUIO[Unit] = for {
    ctx <- ZIO.service[WalletContext]
    _   <- run(deleteQuery(ctx.wallet, id)).orDie // TODO: handle error and return CanNotDeleteAccountsGroup
  } yield ()

  override def group[T <: Groupable](`type`: Group.Type, getItemsFn: WalletUIO[List[T]]): WalletUIO[List[Grouped[T]]] =
    for {
      ctx     <- ZIO.service[WalletContext]
      groups  <- list(`type`)
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
}

object GroupsServiceLive {
  val layer = ZLayer.fromFunction(GroupsServiceLive.apply _)
}
