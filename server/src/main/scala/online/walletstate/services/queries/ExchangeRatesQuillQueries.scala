package online.walletstate.services.queries

import online.walletstate.common.models.{Asset, ExchangeRate}

trait ExchangeRatesQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*

  protected inline def insert(rate: ExchangeRate): Quoted[Insert[ExchangeRate]] =
    quote(Tables.ExchangeRates.insertValue(lift(rate)))

  protected inline def rateById(id: ExchangeRate.Id): Quoted[EntityQuery[ExchangeRate]] =
    quote(Tables.ExchangeRates.filter(_.id == lift(id)))

  protected inline def ratesByAssets(from: Asset.Id, to: Asset.Id): Quoted[EntityQuery[ExchangeRate]] =
    quote(Tables.ExchangeRates.filter(_.from == lift(from)).filter(_.to == lift(to)))
}
