package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.RecordNotExist
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.models.api.{FullRecord, RecordData, SingleTransactionRecord}
import online.walletstate.models.{Record, Account, AssetBalance, Page, Transaction, Wallet}
import zio.{Task, ZIO, ZLayer}

trait RecordsService {
  def create(wallet: Wallet.Id, data: RecordData): Task[FullRecord]
  def get(wallet: Wallet.Id, id: Record.Id): Task[FullRecord]
  def list(wallet: Wallet.Id, account: Account.Id, pageToken: Option[Page.Token]): Task[Page[FullRecord]]
  def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetBalance]]
}

case class RecordsServiceLive(quill: WalletStateQuillContext) extends RecordsService {
  import io.getquill.*
  import quill.{*, given}
  import io.getquill.extras.ZonedDateTimeOps

  private inline val PageSize = 50 // TODO Move to configs

  override def create(wallet: Wallet.Id, data: RecordData): Task[FullRecord] = for {
    // TODO check record is for current wallet
    recordWithTransactions <- Record.make(data)
    (record, transactions) = recordWithTransactions
    _ <- transaction(run(insertRecord(record)) *> run(insertTransactions(transactions)))
  } yield record.toFull(transactions)

  override def get(wallet: Wallet.Id, id: Record.Id): Task[FullRecord] = for {
    // TODO check record is for current wallet
    record       <- run(getRecord(id)).headOrError(RecordNotExist)
    transactions <- run(transactionsById(id))
  } yield record.toFull(transactions)

  override def list(wallet: Wallet.Id, account: Account.Id, pageToken: Option[Page.Token]): Task[Page[FullRecord]] = {
    val records = pageToken match {
      case Some(value) => run(selectFullRecords(account).page(PageSize, value))
      case None        => run(selectFullRecords(account).firstPage(PageSize))
    }

    records
      .map(_.map { case ((record, transaction1), transaction2) => record.toFull(transaction1, transaction2) })
      .map(r => page(r, r.size < PageSize))
  }

  override def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetBalance]] = for {
    // TODO check account is for current wallet
    balances <- run(balanceByAccount(account))
  } yield balances

  private def page(records: List[FullRecord], isLastPage: Boolean): Page[FullRecord] = {
    if (isLastPage) Page(records, None)
    else {
      val lastRecord = records.lastOption
      val pageToken  = lastRecord.map(r => Page.Token(r.id, r.datetime))
      Page(records, pageToken)
    }
  }

  // queries utils
  // TODO needs for `quote(transaction.id <= lift(id))`. Investigate more general options for AnyVal
  implicit class RecordIdOrdered(val value: Record.Id) extends Ordered[Record.Id] {
    override def compare(that: Record.Id): Index = value.id.compareTo(that.id)
  }

  extension (recordsQuery: Query[((Record, Transaction), Option[Transaction])]) {
    inline def firstPage(pageSize: Int): Query[((Record, Transaction), Option[Transaction])] = quote {
      recordsQuery
        .sortBy { case ((r, _), _) => (r.datetime, r.id) }(Ord(Ord.desc, Ord.asc))
        .take(pageSize)
    }

    inline def page(pageSize: Int, pageToken: Page.Token): Query[((Record, Transaction), Option[Transaction])] = quote {
      recordsQuery
        .filter { case ((r, _), _) =>
          r.datetime < lift(pageToken.dt) || (r.datetime == lift(pageToken.dt) && r.id > lift(pageToken.id))
        }
        .sortBy { case ((r, _), _) => (r.datetime, r.id) }(Ord(Ord.desc, Ord.asc))
        .take(pageSize)
    }
  }

  // queries
  private inline def insertRecord(record: Record) = Tables.Records.insertValue(lift(record))
  private inline def getRecord(id: Record.Id)     = Tables.Records.filter(_.id == lift(id))

  private inline def selectFullRecords(account: Account.Id): Query[((Record, Transaction), Option[Transaction])] =
    Tables.Records
      .join(Tables.Transactions)
      .on(_.id == _.id)
      .leftJoin(Tables.Transactions)
      .on { case ((record, t1), t2) => record.id == t2.id && (t1.account != t2.account || t1.asset != t2.asset) }
      .filter { case ((record, t1), t2) => t1.account == lift(account) }
      .distinctOn { case ((record, t1), t2) => (record.datetime, record.id) } //must be the same as sortBy

  private inline def insertTransactions(transactions: List[Transaction]) =
    quote(liftQuery(transactions).foreach(t => Tables.Transactions.insertValue(t)))

  private inline def transactionsById(id: Record.Id) = Tables.Transactions.filter(_.id == lift(id))

  private inline def transactionsByAccount(account: Account.Id): Query[Transaction] =
    Tables.Transactions.filter(_.account == lift(account))

  private inline def balanceByAccount(account: Account.Id) =
    Tables.Transactions
      .filter(_.account == lift(account))
      .groupByMap(_.asset)(t => AssetBalance(t.asset, sum(t.amount)))
}

object RecordsServiceLive {
  val layer = ZLayer.fromFunction(RecordsServiceLive.apply _)
}
