package online.walletstate.models

import online.walletstate.models.api.CreateExchangeRate
import zio.http.codec.PathCodec
import zio.{Random, Task, UIO, ZIO}
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.ZonedDateTime
import java.util.UUID

final case class ExchangeRate(
    id: ExchangeRate.Id,
    from: Asset.Id,
    to: Asset.Id,
    rate: BigDecimal,
    datetime: ZonedDateTime
)

object ExchangeRate {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("exchange-rate-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(wallet: Wallet.Id, info: CreateExchangeRate): UIO[ExchangeRate] =
    Id.random.map(ExchangeRate(_, info.from, info.to, info.rate, info.datetime))

  given codec: JsonCodec[ExchangeRate] = DeriveJsonCodec.gen[ExchangeRate]
}