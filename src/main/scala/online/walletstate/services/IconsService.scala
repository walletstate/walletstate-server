package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.{IconNotExist, InvalidIconId}
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.{AppError, Icon}
import online.walletstate.services.queries.IconsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait IconsService {
  def create(data: Icon.Data): WalletIO[InvalidIconId, Icon]
  def get(id: Icon.Id): WalletIO[IconNotExist, Icon]
  def listIds(tag: Option[String]): WalletUIO[List[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService with IconsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(data: Icon.Data): WalletIO[InvalidIconId, Icon] = for {
    ctx            <- ZIO.service[WalletContext]
    icon           <- Icon.make(ctx.wallet, data.contentType, data.content, data.tags)
    maybeExistTags <- run(selectForCurrent(ctx.wallet, icon.id).map(_.tags)).orDie
    iconWithUpdatedTags = icon.copy(tags = (icon.tags ::: maybeExistTags.flatten).distinct)
    _ <- transaction(run(selectForCurrent(ctx.wallet, icon.id).delete) *> run(insert(iconWithUpdatedTags))).orDie
  } yield icon

  override def get(id: Icon.Id): WalletIO[IconNotExist, Icon] = for {
    ctx  <- ZIO.service[WalletContext]
    icon <- run(selectForCurrentOrDefault(ctx.wallet, id)).orDie.headOrError(IconNotExist(id))
  } yield icon

  override def listIds(maybeTag: Option[String]): WalletUIO[List[Icon.Id]] =
    ZIO.serviceWithZIO[WalletContext] { ctx =>
      maybeTag match {
        case Some(tag) => run(selectIdsWithTag(ctx.wallet, tag)).orDie // icons with tag from current wallet and general
        case None      => run(selectIds(ctx.wallet)).orDie             // list wallet only icons
      }
    }

}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}
