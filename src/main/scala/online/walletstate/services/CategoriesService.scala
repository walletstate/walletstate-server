package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateCategory, Grouped, UpdateCategory}
import online.walletstate.models.{AppError, Category, Group, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait CategoriesService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateCategory): Task[Category]
  def get(wallet: Wallet.Id, id: Category.Id): Task[Category]
  def list(wallet: Wallet.Id): Task[List[Category]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Category]]]
  def update(wallet: Wallet.Id, id: Category.Id, info: UpdateCategory): Task[Category]
}

final case class CategoriesServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends CategoriesService {
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

  override def update(wallet: Wallet.Id, id: Category.Id, info: UpdateCategory): Task[Category] = for {
    // TODO check category is in wallet. check update result
    _ <- run(update(id, info))
  } yield Category(id, info.group, info.name, info.icon, info.tags, info.idx)

  // queries
  private inline def insert(category: Category) = quote(Tables.Categories.insertValue(lift(category)))
  private inline def categoriesByWallet(wallet: Wallet.Id) = quote {
    Tables.Categories
      .join(Tables.Groups)
      .on(_.group == _.id)
      .filter { case (_, group) => group.wallet == lift(wallet) }
      .map { case (group, _) => group }
  }
  private inline def categoriesById(wallet: Wallet.Id, id: Category.Id) =
    categoriesByWallet(wallet).filter(_.id == lift(id))

  private inline def update(id: Category.Id, info: UpdateCategory) =
    Tables.Categories
      .filter(_.id == lift(id))
      .update(
        _.group -> lift(info.group),
        _.name  -> lift(info.name),
        _.icon  -> lift(info.icon),
        _.tags  -> lift(info.tags),
        _.idx   -> lift(info.idx)
      )
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
