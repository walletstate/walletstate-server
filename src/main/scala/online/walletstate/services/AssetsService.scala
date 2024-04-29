package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateAsset, UpdateAsset}
import online.walletstate.models.{AppError, Asset, User, Wallet}
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Chunk, Task, ZLayer}

trait AssetsService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAsset): Task[Asset]
  def get(wallet: Wallet.Id, id: Asset.Id): Task[Asset]
  def list(wallet: Wallet.Id): Task[List[Asset]]
  def update(wallet: Wallet.Id, id: Asset.Id, info: UpdateAsset): Task[Unit]
}

final case class AssetsServiceLive(quill: WalletStateQuillContext) extends AssetsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAsset): Task[Asset] = for {
    asset <- Asset.make(wallet, info)
    _     <- run(insert(asset))
  } yield asset

  override def get(wallet: Wallet.Id, id: Asset.Id): Task[Asset] =
    run(assetsById(wallet, id)).headOrError(AppError.AssetNotExist)

  override def list(wallet: Wallet.Id): Task[List[Asset]] = run(assetsByWallet(wallet))

  override def update(wallet: Wallet.Id, id: Asset.Id, info: UpdateAsset): Task[Unit] =
    run(updateQuery(wallet, id, info)).map(_ => ()) // TODO check result and return error if not found

  //  queries
  private inline def insert(asset: Asset)                        = quote(Tables.Assets.insertValue(lift(asset)))
  private inline def assetsByWallet(wallet: Wallet.Id)           = quote(Tables.Assets.filter(_.wallet == lift(wallet)))
  private inline def assetsById(wallet: Wallet.Id, id: Asset.Id) = assetsByWallet(wallet).filter(_.id == lift(id))

  private inline def updateQuery(wallet: Wallet.Id, asset: Asset.Id, info: UpdateAsset) =
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

object AssetsServiceLive {
  val layer = ZLayer.fromFunction(AssetsServiceLive.apply _)
}
