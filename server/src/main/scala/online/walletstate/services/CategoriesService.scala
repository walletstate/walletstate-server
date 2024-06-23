package online.walletstate.services

import online.walletstate.common.models.{Category, Group, Grouped}
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.CategoryNotExist
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.AppError
import online.walletstate.services.queries.CategoriesQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait CategoriesService {
  def create(info: Category.Data): WalletUIO[Category]
  def get(id: Category.Id): WalletIO[CategoryNotExist, Category]
  def list: WalletUIO[List[Category]]
  def grouped: WalletUIO[List[Grouped[Category]]]
  def update(id: Category.Id, info: Category.Data): WalletUIO[Unit]
}

final case class CategoriesServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends CategoriesService
    with CategoriesQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(info: Category.Data): WalletUIO[Category] = for {
    category <- Category.make(info) // todo: check group exists
    _        <- run(insert(category)).orDie
  } yield category

  override def get(id: Category.Id): WalletIO[CategoryNotExist, Category] = for {
    ctx      <- ZIO.service[WalletContext]
    category <- run(categoriesById(ctx.wallet, id)).orDie.headOrError(CategoryNotExist())
  } yield category

  override def list: WalletUIO[List[Category]] = for {
    ctx        <- ZIO.service[WalletContext]
    categories <- run(categoriesByWallet(ctx.wallet)).orDie
  } yield categories

  override def grouped: WalletUIO[List[Grouped[Category]]] =
    groupsService.group(Group.Type.Categories, list)

  override def update(id: Category.Id, info: Category.Data): WalletUIO[Unit] = for {
    // TODO check category is in wallet. check update result
    _ <- run(updateQuery(id, info)).orDie
  } yield ()
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
