package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{Icon, Wallet}
import zio.{Task, ZLayer}

trait IconsService {
  def create(wallet: Wallet.Id, content: String): Task[Icon]
  def get(wallet: Wallet.Id, id: Icon.Id): Task[String]
  def listIds(wallet: Wallet.Id): Task[Seq[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, content: String): Task[Icon] = for {
    icon <- Icon.make(wallet, content)
    _    <- run(insert(icon))
  } yield icon

  override def get(wallet: Wallet.Id, id: Icon.Id): Task[String] = for {
    icon <- run(selectIcon(wallet, id))
  } yield icon.headOption.map(_.content).getOrElse("default not found image")

  override def listIds(wallet: Wallet.Id): Task[Seq[Icon.Id]] = run(selectIds(wallet))

  //  queries
  private inline def insert(icon: Icon) = quote(query[Icon].insertValue(lift(icon)))
  private inline def selectIcon(wallet: Wallet.Id, id: Icon.Id) =
    quote(query[Icon].filter(_.wallet == lift(wallet)).filter(_.id == lift(id)))
  private inline def selectIds(wallet: Wallet.Id) =
    quote(query[Icon].filter(_.wallet == lift(wallet)).map(_.id))
}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}
