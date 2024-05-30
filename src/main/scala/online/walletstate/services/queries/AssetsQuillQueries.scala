package online.walletstate.services.queries

import online.walletstate.models.{Asset, Wallet}
import online.walletstate.models.api.UpdateAsset

trait AssetsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(asset: Asset) =
    quote(Tables.Assets.insertValue(lift(asset)))

  protected inline def assetsByWallet(wallet: Wallet.Id) =
    Tables.Assets
      .join(Tables.Groups)
      .on(_.group == _.id)
      .filter((asset, group) => group.wallet == lift(wallet))
      .map((asset, group) => asset)

  protected inline def assetsById(wallet: Wallet.Id, id: Asset.Id) =
    assetsByWallet(wallet).filter(_.id == lift(id))

  protected inline def updateQuery(asset: Asset.Id, info: UpdateAsset) =
    Tables.Assets
      .filter(_.id == lift(asset))
      .update(
        _.group          -> lift(info.group),
        _.`type`         -> lift(info.`type`),
        _.ticker         -> lift(info.ticker),
        _.name           -> lift(info.name),
        _.icon           -> lift(info.icon),
        _.tags           -> lift(info.tags),
        _.idx            -> lift(info.idx),
        _.startDate      -> lift(info.startDate),
        _.endDate        -> lift(info.endDate),
        _.lockDuration   -> lift(info.lockDuration),
        _.unlockDuration -> lift(info.unlockDuration),
        _.denominatedIn  -> lift(info.denominatedIn),
        _.denomination   -> lift(info.denomination)
      )
}
