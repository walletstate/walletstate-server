package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.CategoryNotExist
import online.walletstate.models.{Category, Wallet, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Task, ZLayer}

trait CategoriesService {
  def create(wallet: Wallet.Id, name: String, user: User.Id): Task[Category]
  def get(wallet: Wallet.Id, id: Category.Id): Task[Category]
  def list(wallet: Wallet.Id): Task[Seq[Category]]
}

final case class CategoriesServiceLive(quill: QuillCtx) extends CategoriesService {
  import io.getquill.*
  import quill.*

  override def create(wallet: Wallet.Id, name: String, user: User.Id): Task[Category] = for {
    category <- Category.make(wallet, name, user)
    _        <- run(insert(category))
  } yield category

  override def get(wallet: Wallet.Id, id: Category.Id): Task[Category] =
    run(categoriesById(wallet, id)).map(_.headOption).getOrError(CategoryNotExist)

  override def list(wallet: Wallet.Id): Task[Seq[Category]] =
    run(categoriesByNs(wallet))

  // queries
  private inline def categories                       = quote(querySchema[Category]("categories"))
  private inline def insert(category: Category)       = quote(categories.insertValue(lift(category)))
  private inline def categoriesByNs(ns: Wallet.Id) = quote(categories.filter(_.wallet == lift(ns)))
  private inline def categoriesById(ns: Wallet.Id, id: Category.Id) = categoriesByNs(ns).filter(_.id == lift(id))
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
