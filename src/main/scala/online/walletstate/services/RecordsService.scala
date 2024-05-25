package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.RecordNotExist
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.models.api.{FullRecord, RecordData, SingleTransactionRecord}
import online.walletstate.models.{Account, AssetAmount, Page, Record, Transaction, Wallet}
import online.walletstate.services.queries.RecordsQuillQueries
import zio.{Task, ZIO, ZLayer}

trait RecordsService {
  def create(wallet: Wallet.Id, data: RecordData): Task[FullRecord]
  def get(wallet: Wallet.Id, id: Record.Id): Task[FullRecord]
  def update(wallet: Wallet.Id, id: Record.Id, data: RecordData): Task[FullRecord]
  def delete(wallet: Wallet.Id, id: Record.Id): Task[Unit]
  def list(wallet: Wallet.Id, account: Account.Id, pageToken: Option[Page.Token]): Task[Page[FullRecord]]
  def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetAmount]]
}

case class RecordsServiceLive(quill: WalletStateQuillContext) extends RecordsService with RecordsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

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

  override def update(wallet: Wallet.Id, id: Record.Id, data: RecordData): Task[FullRecord] = for {
    // TODO check record is for current wallet
    updatedRecord <- Record.make(id, data)
    (record, transactions) = updatedRecord
    _ <- transaction(
      run(transactionsById(id).delete) *> run(getRecord(id).delete) *>
        run(insertRecord(record)) *> run(insertTransactions(transactions))
    )
  } yield record.toFull(transactions)

  override def delete(wallet: Wallet.Id, id: Record.Id): Task[Unit] =
    // TODO check record is for current wallet and deletion result
    transaction(run(transactionsById(id).delete) *> run(getRecord(id).delete)).map(_ => ())

  override def list(wallet: Wallet.Id, account: Account.Id, pageToken: Option[Page.Token]): Task[Page[FullRecord]] = {
    val records = pageToken match {
      case Some(value) => run(selectFullRecords(account).page(PageSize, value))
      case None        => run(selectFullRecords(account).firstPage(PageSize))
    }

    records
      .map(_.map { case ((record, transaction1), transaction2) => record.toFull(transaction1, transaction2) })
      .map(r => page(r, r.size < PageSize))
  }

  override def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetAmount]] = for {
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

}

object RecordsServiceLive {
  val layer = ZLayer.fromFunction(RecordsServiceLive.apply _)
}
