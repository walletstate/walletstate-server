package online.walletstate.models

import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.errors.{InvalidPageToken, InvalidTransactionInfo}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.http.codec.{PathCodec, QueryCodec}
import zio.json.{DeriveJsonCodec, DeriveJsonEncoder, JsonCodec}
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, Random, Task, UIO, ZIO}

import java.time.ZonedDateTime
import java.util.{Base64, UUID}
import scala.util.Try

final case class Transaction(
    id: Transaction.Id,
    account: Account.Id,
    asset: Asset.Id,
    `type`: Transaction.Type,
    category: Category.Id,
    amount: BigDecimal,
    datetime: ZonedDateTime,
    description: Option[String],
    tags: Chunk[String],
    externalId: Option[String],
    spentOn: Option[Asset.Id],
    generatedBy: Option[Asset.Id]
)

object Transaction {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("transaction-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
    given schema: Schema[Id]   = Schema[UUID].transform(Id(_), _.id)
  }

  enum Type {
    case Income, Spending, Transfer
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr.capitalize)).toEither.left.map(_ => s"$typeStr is not a transaction type")

    def asString(`type`: Type): String = `type`.toString.toLowerCase
  }

  // TODO improve validation and model mapping
  def make(info: CreateTransaction): Task[Seq[Transaction]] = info.`type` match {
    case Type.Income =>
      for {
        id <- Id.random
        to <- ZIO.succeed(info.to).getOrError(InvalidTransactionInfo("Income transaction must have `to` field"))
      } yield Seq(
        Transaction(
          id,
          to.account,
          to.asset,
          info.`type`,
          info.category,
          to.toAmount,
          info.datetime,
          info.description,
          Chunk.from(info.tags),
          info.externalId,
          None,
          info.generatedBy
        )
      )

    case Type.Spending =>
      for {
        id   <- Id.random
        from <- ZIO.succeed(info.from).getOrError(InvalidTransactionInfo("Spending transaction must have `from` field"))
      } yield Seq(
        Transaction(
          id,
          from.account,
          from.asset,
          info.`type`,
          info.category,
          from.fromAmount,
          info.datetime,
          info.description,
          Chunk.from(info.tags),
          info.externalId,
          info.spentOn,
          None
        )
      )

    case Type.Transfer =>
      for {
        id   <- Id.random
        from <- ZIO.succeed(info.from).getOrError(InvalidTransactionInfo("Transfer transaction must have `from` field"))
        to   <- ZIO.succeed(info.to).getOrError(InvalidTransactionInfo("Transfer transaction must have `to` field"))
      } yield Seq(
        Transaction(
          id,
          from.account,
          from.asset,
          info.`type`,
          info.category,
          from.fromAmount,
          info.datetime,
          info.description,
          Chunk.from(info.tags),
          info.externalId,
          info.spentOn,
          None
        ),
        Transaction(
          id,
          to.account,
          to.asset,
          info.`type`,
          info.category,
          to.toAmount,
          info.datetime,
          info.description,
          Chunk.from(info.tags),
          info.externalId,
          None,
          info.generatedBy
        )
      )
  }

  final case class Page(items: Chunk[Transaction], nextPage: Option[Page.Token])

  object Page {

    case class Token private[Transaction] (id: Transaction.Id, dt: ZonedDateTime)
    object Token {

      private val plainCodec = DeriveJsonCodec.gen[Token]

      private def base64Encode(stringJson: String): String =
        Base64.getEncoder.withoutPadding().encodeToString(stringJson.getBytes)

      private def base64Decode(base64String: String): Either[String, String] =
        Try(Base64.getDecoder.decode(base64String)).toEither
          .map(byteArray => new String(byteArray))
          .left
          .map(_.getMessage)

      // TODO make some more compact token
      given schema: Schema[Token] = Schema[String].transformOrFail[Token](
        string => base64Decode(string).flatMap(plainCodec.decoder.decodeJson),
        token => Right(base64Encode(plainCodec.encoder.encodeJson(token).toString))
      )

      val queryCodec: QueryCodec[Token] =
        QueryCodec
          .query("page")
          .transformOrFail { string => base64Decode(string).flatMap(plainCodec.decoder.decodeJson) } { token =>
            Right(base64Encode(plainCodec.encoder.encodeJson(token).toString))
          }
    }

    given schema: Schema[Page] = DeriveSchema.gen[Page]
  }

  def page(transactions: Seq[Transaction], isNotLastPage: Boolean): Page = {
    val token = if (isNotLastPage) transactions.lastOption.map(t => Page.Token(t.id, t.datetime)) else None
    Page(Chunk.from(transactions), token)
  }

  given schema: Schema[Transaction] = DeriveSchema.gen[Transaction]
}
