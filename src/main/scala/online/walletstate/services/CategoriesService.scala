package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateCategory, Grouped}
import online.walletstate.models.{AppError, Category, Group, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait CategoriesService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateCategory): Task[Category]
  def get(wallet: Wallet.Id, id: Category.Id): Task[Category]
  def list(wallet: Wallet.Id): Task[List[Category]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Category]]]
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
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
