package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.CreateAsset
import online.walletstate.models.{AppError, Asset, User, Wallet}
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Chunk, Task, ZLayer}

trait AssetsService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAsset): Task[Asset]
  def get(wallet: Wallet.Id, id: Asset.Id): Task[Asset]
  def list(wallet: Wallet.Id): Task[List[Asset]]
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

//  queries
  private inline def insert(asset: Asset)                        = quote(Tables.Assets.insertValue(lift(asset)))
  private inline def assetsByWallet(wallet: Wallet.Id)           = quote(Tables.Assets.filter(_.wallet == lift(wallet)))
  private inline def assetsById(wallet: Wallet.Id, id: Asset.Id) = assetsByWallet(wallet).filter(_.id == lift(id))
}

object AssetsServiceLive {
  val layer = ZLayer.fromFunction(AssetsServiceLive.apply _)
}
