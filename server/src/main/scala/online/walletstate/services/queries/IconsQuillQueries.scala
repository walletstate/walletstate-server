package online.walletstate.services.queries

import online.walletstate.common.models.{Icon, Wallet}

trait IconsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(icon: Icon): Quoted[Insert[Icon]] =
    quote(query[Icon].insertValue(lift(icon)).onConflictIgnore)

  protected inline def selectForCurrent(wallet: Wallet.Id, id: Icon.Id): Quoted[EntityQuery[Icon]] =
    quote(query[Icon].filter(_.wallet.exists(_ == lift(wallet))).filter(_.id == lift(id)))

  protected inline def selectForCurrentOrDefault(wallet: Wallet.Id, id: Icon.Id): Quoted[EntityQuery[Icon]] =
    quote(query[Icon].filter(_.wallet.filterIfDefined(_ == lift(wallet))).filter(_.id == lift(id)))

  protected inline def selectIds(wallet: Wallet.Id): Quoted[EntityQuery[Icon.Id]] =
    quote(query[Icon].filter(_.wallet.exists(_ == lift(wallet))).map(_.id))

  protected inline def selectIdsWithTag(wallet: Wallet.Id, tag: String): Quoted[EntityQuery[Icon.Id]] =
    quote(query[Icon].filter(_.wallet.filterIfDefined(_ == lift(wallet))).filter(_.tags.contains(lift(tag))).map(_.id))
}
