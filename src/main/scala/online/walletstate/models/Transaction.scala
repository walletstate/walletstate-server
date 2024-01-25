package online.walletstate.models

import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.errors.InvalidTransactionInfo
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.time.ZonedDateTime
import java.util.UUID
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
    tags: Seq[String],
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
  }

  enum Type {
    case Income, Spending, Transfer
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr.capitalize)).toEither.left.map(_ => s"$typeStr is not a transaction type")

    def asString(`type`: Type): String = `type`.toString.toLowerCase

    given codec: JsonCodec[Type] = JsonCodec[String].transform(t => Type.valueOf(t.capitalize), _.toString.toLowerCase)
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
          info.tags,
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
          info.tags,
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
          info.tags,
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
          info.tags,
          info.externalId,
          None,
          info.generatedBy
        )
      )
  }

  given codec: JsonCodec[Transaction] = DeriveJsonCodec.gen[Transaction]
}
