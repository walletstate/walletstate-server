package online.walletstate.common.models

import zio.http.codec.PathCodec
import zio.json.JsonCodec
import zio.schema.{derived, Schema}
import zio.{Random, UIO, ZIO}

import java.time.ZonedDateTime
import java.util.UUID
import scala.util.Try

final case class Record(
    id: Record.Id,
    wallet: Wallet.Id,
    `type`: Record.Type,
    category: Category.Id,
    datetime: ZonedDateTime,
    description: Option[String],
    tags: List[String],
    externalId: Option[String],
    spentOn: Option[Asset.Id],
    generatedBy: Option[Asset.Id]
) {

  def toFull(from: Option[Transaction.Data], to: Option[Transaction.Data]): Record.Full =
    Record.Full(id, `type`, from, to, category, datetime, description, tags, externalId, spentOn, generatedBy)

  def toFull(transactions: List[Transaction]): Record.Full =
    toFull(transactions.find(_.isFrom).map(_.data), transactions.find(_.isTo).map(_.data))

  def toFull(t1: Transaction, t2: Option[Transaction]): Record.Full = {
    val from = if (t1.isFrom) Some(t1) else t2.filter(_.isFrom)
    val to   = if (t1.isTo) Some(t1) else t2.filter(_.isTo)
    toFull(from.map(_.data), to.map(_.data))
  }

  def toSingleTransaction(transaction: Transaction.Data): Record.SingleTransaction =
    Record.SingleTransaction(
      id,
      `type`,
      transaction,
      category,
      datetime,
      description,
      tags,
      externalId,
      spentOn,
      generatedBy
    )
}

object Record {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("record-id").transform(Id(_))(_.id)

    given schema: Schema[Id]   = Schema[UUID].transform(Id(_), _.id)
    given codec: JsonCodec[Id] = zio.schema.codec.JsonCodec.jsonCodec(schema)
  }

  enum Type {
    case Income, Spending, Transfer
  }

  object Type {
    def fromString(typeStr: String): Either[String, Type] =
      Try(Type.valueOf(typeStr)).toEither.left.map(_ => s"$typeStr is not a record type")

    def asString(`type`: Type): String = `type`.toString
  }

  def make(wallet: Wallet.Id, data: Data): UIO[(Record, List[Transaction])] = Id.random.flatMap(make(wallet, _, data))

  def make(wallet: Wallet.Id, id: Record.Id, d: Data): UIO[(Record, List[Transaction])] = ZIO.succeed {
    val r = Record(
      id,
      wallet,
      d.`type`,
      d.category,
      d.datetime,
      d.description,
      d.tags,
      d.externalId,
      d.spentOn,
      d.generatedBy
    )
    (r, Transaction.make(wallet, id, d.from, d.to))
  }

  final case class Data(
      `type`: Record.Type,
      from: Option[Transaction.Data],
      to: Option[Transaction.Data],
      category: Category.Id,
      datetime: ZonedDateTime,
      description: Option[String],
      tags: List[String],
      externalId: Option[String],
      spentOn: Option[Asset.Id],
      generatedBy: Option[Asset.Id]
  ) derives Schema

  final case class SingleTransaction(
      id: Record.Id,
      `type`: Record.Type,
      transaction: Transaction.Data,
      category: Category.Id,
      datetime: ZonedDateTime,
      description: Option[String],
      tags: List[String],
      externalId: Option[String],
      spentOn: Option[Asset.Id],
      generatedBy: Option[Asset.Id]
  ) derives Schema

  final case class Full(
      id: Record.Id,
      `type`: Record.Type,
      from: Option[Transaction.Data],
      to: Option[Transaction.Data],
      category: Category.Id,
      datetime: ZonedDateTime,
      description: Option[String],
      tags: List[String],
      externalId: Option[String],
      spentOn: Option[Asset.Id],
      generatedBy: Option[Asset.Id]
  ) derives Schema
}
