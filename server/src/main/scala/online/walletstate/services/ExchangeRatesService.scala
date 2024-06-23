package online.walletstate.services

import online.walletstate.common.models.{Asset, ExchangeRate}
import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.ExchangeRateNotExist
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.AppError
import online.walletstate.services.queries.ExchangeRatesQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait ExchangeRatesService {
  def create(info: ExchangeRate.Data): WalletUIO[ExchangeRate]
  def get(id: ExchangeRate.Id): WalletIO[ExchangeRateNotExist, ExchangeRate]
  def list(from: Asset.Id, to: Asset.Id): WalletUIO[List[ExchangeRate]]
}

case class ExchangeRatesServiceLive(quill: WalletStateQuillContext)
    extends ExchangeRatesService
    with ExchangeRatesQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(info: ExchangeRate.Data): WalletUIO[ExchangeRate] = for {
    // TODO: check `from` and `to` assets are in wallet
    ctx <- ZIO.service[WalletContext]
    exchangeRate <- ExchangeRate.make(ctx.wallet, info)
    _            <- run(insert(exchangeRate)).orDie
  } yield exchangeRate

  override def get(id: ExchangeRate.Id): WalletIO[ExchangeRateNotExist, ExchangeRate] = for {
    exchangeRate <- run(rateById(id)).orDie.headOrError(ExchangeRateNotExist())
    // TODO: check `exchange rate` is for current wallet
  } yield exchangeRate

  override def list(from: Asset.Id, to: Asset.Id): WalletUIO[List[ExchangeRate]] = for {
    // TODO: check `from` and `to` assets are in wallet
    rates <- run(ratesByAssets(from, to)).orDie
  } yield rates
}

object ExchangeRatesServiceLive {
  val layer = ZLayer.fromFunction(ExchangeRatesServiceLive.apply _)
}
