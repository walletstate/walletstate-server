package online.walletstate.common.models

import zio.*
import zio.http.codec.{PathCodec, QueryCodec}
import zio.schema.{derived, Schema}

import java.time.ZonedDateTime
import java.util.UUID
import scala.util.Try

final case class Asset(
    id: Asset.Id,
    wallet: Wallet.Id,
    group: Group.Id,
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: Option[Icon.Id],
    tags: List[String],
    idx: Int,
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    lockDuration: Option[Duration],
    unlockDuration: Option[Duration],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
) extends Groupable
    derives Schema

object Asset {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id]                 = zio.http.uuid("asset-id").transform(Id(_))(_.id)
    def query(name: String): QueryCodec[Id] = QueryCodec.queryTo[UUID](name).transform(Id(_))(_.id)

    given schema: Schema[Id] = Schema[UUID].transform(Id(_), _.id)
  }

  enum Type {
    case Fiat, Crypto, Deposit, Bond, Stock, Other
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr)).toEither.left.map(_ => s"$typeStr is not an asset type")

    def asString(`type`: Type): String = `type`.toString
  }

  def make(wallet: Wallet.Id, data: Data): UIO[Asset] =
    Id.random.map(
      Asset(
        _,
        wallet,
        data.group,
        data.`type`,
        data.ticker,
        data.name,
        data.icon,
        data.tags,
        data.idx,
        data.startDate,
        data.endDate,
        data.lockDuration,
        data.unlockDuration,
        data.denominatedIn,
        data.denomination
      )
    )

  final case class Data(
      group: Group.Id,
      `type`: Asset.Type,
      ticker: String,
      name: String,
      icon: Option[Icon.Id],
      tags: List[String],
      idx: Int,
      startDate: Option[ZonedDateTime],
      endDate: Option[ZonedDateTime],
      lockDuration: Option[Duration],
      unlockDuration: Option[Duration],
      denominatedIn: Option[Asset.Id],
      denomination: Option[BigDecimal]
  ) derives Schema
}
