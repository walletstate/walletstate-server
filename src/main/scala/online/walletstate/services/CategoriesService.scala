package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateCategory, Grouped, UpdateCategory}
import online.walletstate.models.{AppError, Category, Group, User, Wallet}
import online.walletstate.services.queries.CategoriesQuillQueries
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait CategoriesService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateCategory): Task[Category]
  def get(wallet: Wallet.Id, id: Category.Id): Task[Category]
  def list(wallet: Wallet.Id): Task[List[Category]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Category]]]
  def update(wallet: Wallet.Id, id: Category.Id, info: UpdateCategory): Task[Unit]
}

final case class CategoriesServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends CategoriesService
    with CategoriesQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateCategory): Task[Category] = for {
    category <- Category.make(info) // todo: check group exists
    _        <- run(insert(category))
  } yield category

  override def get(wallet: Wallet.Id, id: Category.Id): Task[Category] =
    run(categoriesById(wallet, id)).map(_.headOption).getOrError(AppError.CategoryNotExist)

  override def list(wallet: Wallet.Id): Task[List[Category]] =
    run(categoriesByWallet(wallet))

  override def grouped(wallet: Wallet.Id): Task[List[Grouped[Category]]] =
    groupsService.group(wallet, Group.Type.Categories, list(wallet))

  override def update(wallet: Wallet.Id, id: Category.Id, info: UpdateCategory): Task[Unit] = for {
    // TODO check category is in wallet. check update result
    _ <- run(update(id, info))
  } yield ()
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
