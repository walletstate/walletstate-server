package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.errors.RecordNotExist
import online.walletstate.models.{Account, Category, Group, Record, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZLayer}

import java.time.Instant

trait RecordsService {
  def create(
      account: Account.Id,
      amount: BigDecimal,
      `type`: Record.Type,
      category: Category.Id,
      description: Option[String],
      time: Instant,
      createdBy: User.Id
  ): Task[Record]
  def get(wallet: Wallet.Id, id: Record.Id): Task[Record]
  def list(wallet: Wallet.Id): Task[Seq[Record]]
  def list(wallet: Wallet.Id, account: Account.Id): Task[Seq[Record]]
}

case class RecordsServiceLive(quill: WalletStateQuillContext) extends RecordsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(
      account: Account.Id,
      amount: BigDecimal,
      `type`: Record.Type,
      category: Category.Id,
      description: Option[String],
      time: Instant,
      createdBy: User.Id
  ): Task[Record] = for {
    record <- Record.make(account, amount, `type`, category, description, time, createdBy)
    _      <- run(insert(record))
  } yield record

  override def get(wallet: Wallet.Id, id: Record.Id): Task[Record] =
    run(recordsById(wallet, id)).map(_.headOption).getOrError(RecordNotExist)

  override def list(wallet: Wallet.Id): Task[Seq[Record]] =
    run(recordsByWallet(wallet))

  override def list(wallet: Wallet.Id, account: Account.Id): Task[Seq[Record]] =
    run(recordsByAccount(wallet, account))


  // queries
  private inline def insert(record: Record) = quote(query[Record].insertValue(lift(record)))

  private inline def recordsByWallet(ns: Wallet.Id) = quote {
    query[Record]
      .join(query[Account])
      .on(_.account == _.id)
      .join(query[Group])
      .on(_._2.group == _.id)
      .filter { case (_, group) => group.wallet == lift(ns) }
      .map { case ((record, _), _) => record }
  }

  private inline def recordsByAccount(ns: Wallet.Id, account: Account.Id) =
    recordsByWallet(ns).filter(_.account == lift(account))

  private inline def recordsById(ns: Wallet.Id, id: Record.Id) =
    recordsByWallet(ns).filter(_.id == lift(id))
}

object RecordsServiceLive {
  val layer = ZLayer.fromFunction(RecordsServiceLive.apply _)
}
