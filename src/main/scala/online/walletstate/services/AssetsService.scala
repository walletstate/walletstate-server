package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.AssetNotExist
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.*
import online.walletstate.services.queries.AssetsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait AssetsService {
  def create(data: Asset.Data): WalletUIO[Asset]
  def get(id: Asset.Id): WalletIO[AssetNotExist, Asset]
  def list: WalletUIO[List[Asset]]
  def grouped: WalletUIO[List[Grouped[Asset]]]
  def update(id: Asset.Id, info: Asset.Data): WalletUIO[Unit]
}

final case class AssetsServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends AssetsService
    with AssetsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(info: Asset.Data): WalletUIO[Asset] = for {
    ctx   <- ZIO.service[WalletContext]
    asset <- Asset.make(info)
    _     <- run(insert(asset)).orDie
  } yield asset

  override def get(id: Asset.Id): WalletIO[AssetNotExist, Asset] = for {
    ctx   <- ZIO.service[WalletContext]
    asset <- run(assetsById(ctx.wallet, id)).orDie.headOrError(AssetNotExist())
  } yield asset

  override def list: WalletUIO[List[Asset]] = for {
    ctx    <- ZIO.service[WalletContext]
    assets <- run(assetsByWallet(ctx.wallet)).orDie
  } yield assets

  override def grouped: WalletUIO[List[Grouped[Asset]]] =
    groupsService.group(Group.Type.Assets, list)

  override def update(id: Asset.Id, info: Asset.Data): WalletUIO[Unit] =
    // TODO check asset is in wallet; check result and return error if not found
    run(updateQuery(id, info)).orDie.map(_ => ())
}

object AssetsServiceLive {
  val layer = ZLayer.fromFunction(AssetsServiceLive.apply _)
}
