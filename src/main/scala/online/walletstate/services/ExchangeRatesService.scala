package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.{Asset, ExchangeRate, Wallet}
import online.walletstate.models.api.CreateExchangeRate
import online.walletstate.models.errors.ExchangeRateNotExist
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

trait ExchangeRatesService {
  def create(wallet: Wallet.Id, info: CreateExchangeRate): Task[ExchangeRate]
  def get(wallet: Wallet.Id, id: ExchangeRate.Id): Task[ExchangeRate]
  def list(wallet: Wallet.Id, from: Asset.Id, to: Asset.Id): Task[Seq[ExchangeRate]]
}

case class ExchangeRatesServiceLive(quill: WalletStateQuillContext) extends ExchangeRatesService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, info: CreateExchangeRate): Task[ExchangeRate] = for {
    // TODO: check `from` and `to` assets are in wallet
    exchangeRate <- ExchangeRate.make(wallet, info)
    _            <- run(insert(exchangeRate))
  } yield exchangeRate

  override def get(wallet: Wallet.Id, id: ExchangeRate.Id): Task[ExchangeRate] = for {
    exchangeRate <- run(rateById(id)).map(_.headOption).getOrError(ExchangeRateNotExist)
    // TODO: check `exchange rate` is for current wallet
  } yield exchangeRate

  override def list(wallet: Wallet.Id, from: Asset.Id, to: Asset.Id): Task[Seq[ExchangeRate]] = for {
    // TODO: check `from` and `to` assets are in wallet
    rates <- run(ratesByAssets(from, to))
  } yield rates

//  queries
  private inline def insert(rate: ExchangeRate)    = quote(Tables.ExchangeRates.insertValue(lift(rate)))
  private inline def rateById(id: ExchangeRate.Id) = quote(Tables.ExchangeRates.filter(_.id == lift(id)))
  private inline def ratesByAssets(from: Asset.Id, to: Asset.Id) = quote(
    Tables.ExchangeRates.filter(_.from == lift(from)).filter(_.to == lift(to))
  )

}

object ExchangeRatesServiceLive {
  val layer = ZLayer.fromFunction(ExchangeRatesServiceLive.apply _)
}
