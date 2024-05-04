package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{AppError, Asset, ExchangeRate, Wallet}
import online.walletstate.models.api.CreateExchangeRate
import online.walletstate.services.queries.ExchangeRatesQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import zio.{Task, ZLayer}

trait ExchangeRatesService {
  def create(wallet: Wallet.Id, info: CreateExchangeRate): Task[ExchangeRate]
  def get(wallet: Wallet.Id, id: ExchangeRate.Id): Task[ExchangeRate]
  def list(wallet: Wallet.Id, from: Asset.Id, to: Asset.Id): Task[List[ExchangeRate]]
}

case class ExchangeRatesServiceLive(quill: WalletStateQuillContext)
    extends ExchangeRatesService
    with ExchangeRatesQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, info: CreateExchangeRate): Task[ExchangeRate] = for {
    // TODO: check `from` and `to` assets are in wallet
    exchangeRate <- ExchangeRate.make(wallet, info)
    _            <- run(insert(exchangeRate))
  } yield exchangeRate

  override def get(wallet: Wallet.Id, id: ExchangeRate.Id): Task[ExchangeRate] = for {
    exchangeRate <- run(rateById(id)).headOrError(AppError.ExchangeRateNotExist)
    // TODO: check `exchange rate` is for current wallet
  } yield exchangeRate

  override def list(wallet: Wallet.Id, from: Asset.Id, to: Asset.Id): Task[List[ExchangeRate]] = for {
    // TODO: check `from` and `to` assets are in wallet
    rates <- run(ratesByAssets(from, to))
  } yield rates
}

object ExchangeRatesServiceLive {
  val layer = ZLayer.fromFunction(ExchangeRatesServiceLive.apply _)
}
