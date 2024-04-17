package online.walletstate.models

import online.walletstate.models.api.CreateAsset
import zio.*
import zio.http.codec.{PathCodec, QueryCodec}
import zio.json.{DeriveJsonCodec, JsonCodec, JsonFieldEncoder}
import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime
import java.util.UUID
import scala.util.Try

final case class Asset(
    id: Asset.Id,
    wallet: Wallet.Id,
    `type`: Asset.Type,
    ticker: String,
    name: String,
    icon: Option[Icon.Id],
    tags: Chunk[String],
    startDate: Option[ZonedDateTime],
    endDate: Option[ZonedDateTime],
    denominatedIn: Option[Asset.Id],
    denomination: Option[BigDecimal]
)

object Asset {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("asset-id").transform(Id(_))(_.id)
    def query(name: String): QueryCodec[Id] = QueryCodec.queryTo[UUID](name).transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
    given schema: Schema[Id] = Schema[UUID].transform(Id(_), _.id)

    given fieldEncoder: JsonFieldEncoder[Id] = JsonFieldEncoder.string.contramap(_.id.toString)
  }

  enum Type {
    case Fiat, Crypto, Deposit, Bond, Stock, Other
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr.capitalize)).toEither.left.map(_ => s"$typeStr is not an asset type")

    def asString(`type`: Type): String = `type`.toString.toLowerCase

    // TODO investigate 500 response for invalid asset type in path
    val path: PathCodec[Type] = zio.http.string("asset-type").transformOrFailLeft(fromString)(asString)

    given codec: JsonCodec[Type] = JsonCodec[String].transformOrFail(fromString, asString)
  }

  def make(wallet: Wallet.Id, info: CreateAsset): UIO[Asset] =
    Id.random.map(
      Asset(
        _,
        wallet,
        info.`type`,
        info.ticker,
        info.name,
        info.icon,
        info.tags,
        info.startDate,
        info.endDate,
        info.denominatedIn,
        info.denomination
      )
    )

  given codec: JsonCodec[Asset] = DeriveJsonCodec.gen[Asset]
  given schema: Schema[Asset] = DeriveSchema.gen[Asset]
}
