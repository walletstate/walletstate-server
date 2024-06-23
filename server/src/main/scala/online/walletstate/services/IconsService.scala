package online.walletstate.services

import online.walletstate.common.models.{Icon, Wallet}
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.{IconNotExist, InvalidIconId}
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.AppError
import online.walletstate.services.queries.IconsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{UIO, ZIO, ZLayer}

import java.security.MessageDigest
import java.util.HexFormat

trait IconsService {
  def create(data: Icon.Data): WalletUIO[Icon]
  def get(id: Icon.Id): WalletIO[IconNotExist, Icon]
  def listIds(tag: Option[String]): WalletUIO[List[Icon.Id]]
}

final case class IconsServiceDBLive(quill: WalletStateQuillContext) extends IconsService with IconsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(data: Icon.Data): WalletUIO[Icon] = for {
    ctx            <- ZIO.service[WalletContext]
    icon           <- make(ctx.wallet, data.contentType, data.content, data.tags)
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

  private def make(wallet: Wallet.Id, contentType: String, content: String, tags: List[String]): UIO[Icon] =
    for {
      contentDigest <- ZIO.succeed(MessageDigest.getInstance("SHA-256").digest(content.getBytes("UTF-8")))
      contentHash   <- ZIO.succeed(HexFormat.of().formatHex(contentDigest))
      iconId        <- ZIO.fromEither(Icon.Id.make(contentHash)).mapError(e => InvalidIconId(e)).orDie
    } yield Icon(Some(wallet), iconId, contentType, content, tags)

}

object IconsServiceDBLive {
  val layer = ZLayer.fromFunction(IconsServiceDBLive.apply _)
}
