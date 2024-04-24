package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.CreateIcon
import online.walletstate.models.{AppError, Icon, Wallet}
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Chunk, Task, ZLayer}

trait IconsService {
  def create(wallet: Wallet.Id, data: CreateIcon): Task[Icon]
  def get(wallet: Wallet.Id, id: Icon.Id): Task[Icon]
  def listIds(wallet: Wallet.Id): Task[List[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, data: CreateIcon): Task[Icon] = for {
    icon <- Icon.make(wallet, data.contentType, data.content, data.tags)
    _    <- run(insert(icon))
  } yield icon

  override def get(wallet: Wallet.Id, id: Icon.Id): Task[Icon] = for {
    icon <- run(selectIcon(wallet, id)).headOrError(AppError.IconNotFount(id))
  } yield icon

  override def listIds(wallet: Wallet.Id): Task[List[Icon.Id]] = run(selectIds(wallet))

  //  queries
  private inline def insert(icon: Icon) = quote(query[Icon].insertValue(lift(icon)).onConflictIgnore)
  private inline def selectIcon(wallet: Wallet.Id, id: Icon.Id) =
    quote(query[Icon].filter(_.wallet.filterIfDefined(_ == lift(wallet))).filter(_.id == lift(id)))
  private inline def selectIds(wallet: Wallet.Id) =
    quote(query[Icon].filter(_.wallet.exists(_ == lift(wallet))).map(_.id))
}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}
