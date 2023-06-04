package online.walletstate.services

import online.walletstate.db.QuillCtx
import online.walletstate.models.errors.RecordNotExist
import online.walletstate.models.{Account, Category, Namespace, Record, RecordType, User}
import online.walletstate.utils.ZIOExtentions.getOrError
import zio.{Task, ZLayer}

import java.time.Instant

trait RecordsService {
  def create(
      account: Account.Id,
      amount: BigDecimal,
      `type`: RecordType,
      category: Category.Id,
      description: Option[String],
      time: Instant,
      createdBy: User.Id
  ): Task[Record]
  def get(namespace: Namespace.Id, id: Record.Id): Task[Record]
  def list(namespace: Namespace.Id): Task[Seq[Record]]
  def list(namespace: Namespace.Id, account: Account.Id): Task[Seq[Record]]
}

case class RecordsServiceLive(quill: QuillCtx) extends RecordsService {
  import io.getquill.*
  import quill.*

  override def create(
      account: Account.Id,
      amount: BigDecimal,
      `type`: RecordType,
      category: Category.Id,
      description: Option[String],
      time: Instant,
      createdBy: User.Id
  ): Task[Record] = for {
    record <- Record.make(account, amount, `type`, category, description, time, createdBy)
    _      <- run(insert(record))
  } yield record

  override def get(namespace: Namespace.Id, id: Record.Id): Task[Record] =
    run(recordsById(namespace, id)).map(_.headOption).getOrError(RecordNotExist)

  override def list(namespace: Namespace.Id): Task[Seq[Record]] =
    run(recordsByNamespace(namespace))

  override def list(namespace: Namespace.Id, account: Account.Id): Task[Seq[Record]] =
    run(recordsByAccount(namespace, account))

  // mappers
  given encodeRecordType: MappedEncoding[RecordType, String] = MappedEncoding[RecordType, String](_.toString)
  given decodeRecordType: MappedEncoding[String, RecordType] = MappedEncoding[String, RecordType](RecordType.valueOf(_))

  // queries
  private inline def insert(record: Record) = quote(query[Record].insertValue(lift(record)))

  private inline def recordsByNamespace(ns: Namespace.Id) = quote {
    query[Record]
      .join(query[Account])
      .on(_.account == _.id)
      .filter { case (_, account) => account.namespace == lift(ns) }
      .map { case (record, _) => record }
  }

  private inline def recordsByAccount(ns: Namespace.Id, account: Account.Id) =
    recordsByNamespace(ns).filter(_.account == lift(account))

  private inline def recordsById(ns: Namespace.Id, id: Record.Id) =
    recordsByNamespace(ns).filter(_.id == lift(id))
}

object RecordsServiceLive {
  val layer = ZLayer.fromFunction(RecordsServiceLive.apply _)
}
