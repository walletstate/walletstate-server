package online.walletstate.services.queries

import online.walletstate.models.{Category, Wallet}
import online.walletstate.models.api.UpdateCategory

trait CategoriesQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(category: Category): Quoted[Insert[Category]] =
    quote(Tables.Categories.insertValue(lift(category)))

  protected inline def categoriesByWallet(wallet: Wallet.Id): Quoted[Query[Category]] = quote {
    Tables.Categories
      .join(Tables.Groups)
      .on(_.group == _.id)
      .filter { case (_, group) => group.wallet == lift(wallet) }
      .map { case (group, _) => group }
  }

  protected inline def categoriesById(wallet: Wallet.Id, id: Category.Id): Query[Category] =
    categoriesByWallet(wallet).filter(_.id == lift(id))

  protected inline def updateQuery(id: Category.Id, info: UpdateCategory): Update[Category] =
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
