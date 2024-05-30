package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.{CreateAsset, Grouped, UpdateAsset}
import online.walletstate.models.{AppError, Asset, Group, User, Wallet}
import online.walletstate.services.queries.AssetsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Task, ZLayer}

trait AssetsService {
  def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAsset): Task[Asset]
  def get(wallet: Wallet.Id, id: Asset.Id): Task[Asset]
  def list(wallet: Wallet.Id): Task[List[Asset]]
  def grouped(wallet: Wallet.Id): Task[List[Grouped[Asset]]]
  def update(wallet: Wallet.Id, id: Asset.Id, info: UpdateAsset): Task[Unit]
}

final case class AssetsServiceLive(quill: WalletStateQuillContext, groupsService: GroupsService)
    extends AssetsService
    with AssetsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, createdBy: User.Id, info: CreateAsset): Task[Asset] = for {
    asset <- Asset.make(wallet, info)
    _     <- run(insert(asset))
  } yield asset

  override def get(wallet: Wallet.Id, id: Asset.Id): Task[Asset] =
    run(assetsById(wallet, id)).headOrError(AppError.AssetNotExist)

  override def list(wallet: Wallet.Id): Task[List[Asset]] = run(assetsByWallet(wallet))

  override def grouped(wallet: Wallet.Id): Task[List[Grouped[Asset]]] =
    groupsService.group(wallet, Group.Type.Assets, list(wallet))

  override def update(wallet: Wallet.Id, id: Asset.Id, info: UpdateAsset): Task[Unit] =
    run(updateQuery(id, info)).map(_ => ()) // TODO check asset is in wallet; check result and return error if not found

}

object AssetsServiceLive {
  val layer = ZLayer.fromFunction(AssetsServiceLive.apply _)
}
