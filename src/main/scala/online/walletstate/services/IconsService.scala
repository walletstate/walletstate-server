package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.CreateIcon
import online.walletstate.models.{AppError, Icon, Wallet}
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Task, ZLayer}

trait IconsService {
  def create(wallet: Wallet.Id, data: CreateIcon): Task[Icon]
  def get(wallet: Wallet.Id, id: Icon.Id): Task[Icon]
  def listIds(wallet: Wallet.Id, tag: Option[String]): Task[List[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, data: CreateIcon): Task[Icon] = for {
    icon           <- Icon.make(wallet, data.contentType, data.content, data.tags)
    maybeExistTags <- run(selectForCurrent(wallet, icon.id).map(_.tags))
    iconWithUpdatedTags = icon.copy(tags = (icon.tags ::: maybeExistTags.flatten).distinct)
    _ <- transaction(run(selectForCurrent(wallet, icon.id).delete) *> run(insert(iconWithUpdatedTags)))
  } yield icon

  override def get(wallet: Wallet.Id, id: Icon.Id): Task[Icon] = for {
    icon <- run(selectForCurrentOrDefault(wallet, id)).headOrError(AppError.IconNotFount(id))
  } yield icon

  override def listIds(wallet: Wallet.Id, maybeTag: Option[String]): Task[List[Icon.Id]] = maybeTag match {
    case Some(tag) => run(selectIdsWithTag(wallet, tag)) // list icons with tag from current wallet and general
    case None      => run(selectIds(wallet))             // list wallet only icons
  }
  //  queries
  private inline def insert(icon: Icon) =
    quote(query[Icon].insertValue(lift(icon)).onConflictIgnore)
  private inline def selectForCurrent(wallet: Wallet.Id, id: Icon.Id) =
    quote(query[Icon].filter(_.wallet.exists(_ == lift(wallet))).filter(_.id == lift(id)))

  private inline def selectForCurrentOrDefault(wallet: Wallet.Id, id: Icon.Id) =
    quote(query[Icon].filter(_.wallet.filterIfDefined(_ == lift(wallet))).filter(_.id == lift(id)))
  private inline def selectIds(wallet: Wallet.Id) =
    quote(query[Icon].filter(_.wallet.exists(_ == lift(wallet))).map(_.id))

  private inline def selectIdsWithTag(wallet: Wallet.Id, tag: String) =
    quote(query[Icon].filter(_.wallet.filterIfDefined(_ == lift(wallet))).filter(_.tags.contains(lift(tag))).map(_.id))
}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}
