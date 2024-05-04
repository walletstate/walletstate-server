package online.walletstate.services.queries

import online.walletstate.models.{Asset, Wallet}
import online.walletstate.models.api.UpdateAsset

trait AssetsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(asset: Asset): Quoted[Insert[Asset]] =
    quote(Tables.Assets.insertValue(lift(asset)))

  protected inline def assetsByWallet(wallet: Wallet.Id): Quoted[EntityQuery[Asset]] =
    quote(Tables.Assets.filter(_.wallet == lift(wallet)))

  protected inline def assetsById(wallet: Wallet.Id, id: Asset.Id): EntityQuery[Asset] =
    assetsByWallet(wallet).filter(_.id == lift(id))

  protected inline def updateQuery(wallet: Wallet.Id, asset: Asset.Id, info: UpdateAsset): Update[Asset] =
    Tables.Assets
      .filter(_.wallet == lift(wallet))
      .filter(_.id == lift(asset))
      .update(
        _.`type`        -> lift(info.`type`),
        _.ticker        -> lift(info.ticker),
        _.name          -> lift(info.name),
        _.icon          -> lift(info.icon),
        _.tags          -> lift(info.tags),
        _.startDate     -> lift(info.startDate),
        _.endDate       -> lift(info.endDate),
        _.denominatedIn -> lift(info.denominatedIn),
        _.denomination  -> lift(info.denomination)
      )
}
