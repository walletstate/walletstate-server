package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.CategoryNotExist
import online.walletstate.models.{Category, Namespace, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Task, ZLayer}

trait CategoriesService {
  def create(namespace: Namespace.Id, name: String, user: User.Id): Task[Category]
  def get(namespace: Namespace.Id, id: Category.Id): Task[Category]
  def list(namespace: Namespace.Id): Task[Seq[Category]]
}

final case class CategoriesServiceLive(quill: QuillCtx) extends CategoriesService {
  import io.getquill.*
  import quill.*

  override def create(namespace: Namespace.Id, name: String, user: User.Id): Task[Category] = for {
    category <- Category.make(namespace, name, user)
    _        <- run(insert(category))
  } yield category

  override def get(namespace: Namespace.Id, id: Category.Id): Task[Category] =
    run(categoriesById(namespace, id)).map(_.headOption).getOrError(CategoryNotExist)

  override def list(namespace: Namespace.Id): Task[Seq[Category]] =
    run(categoriesByNs(namespace))

  // queries
  private inline def categories                       = quote(querySchema[Category]("categories"))
  private inline def insert(category: Category)       = quote(categories.insertValue(lift(category)))
  private inline def categoriesByNs(ns: Namespace.Id) = quote(categories.filter(_.namespace == lift(ns)))
  private inline def categoriesById(ns: Namespace.Id, id: Category.Id) = categoriesByNs(ns).filter(_.id == lift(id))
}

object CategoriesServiceLive {
  val layer = ZLayer.fromFunction(CategoriesServiceLive.apply _)
}
