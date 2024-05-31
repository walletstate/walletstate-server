package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.RecordNotExist
import online.walletstate.models.api.{FullRecord, RecordData}
import online.walletstate.models.{Account, AssetAmount, Page, Record, Transaction}
import online.walletstate.services.queries.RecordsQuillQueries
import online.walletstate.utils.ZIOExtensions.headOrError
import online.walletstate.{WalletIO, WalletUIO}
import zio.{ZIO, ZLayer}

trait RecordsService {
  def create(data: RecordData): WalletUIO[FullRecord]
  def get(id: Record.Id): WalletIO[RecordNotExist, FullRecord]
  def update(id: Record.Id, data: RecordData): WalletUIO[FullRecord]
  def delete(id: Record.Id): WalletUIO[Unit]
  def list(account: Account.Id, pageToken: Option[Page.Token]): WalletUIO[Page[FullRecord]]
  def balance(account: Account.Id): WalletUIO[List[AssetAmount]]
}

case class RecordsServiceLive(quill: WalletStateQuillContext) extends RecordsService with RecordsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  private inline val PageSize = 50 // TODO Move to configs

  override def create(data: RecordData): WalletUIO[FullRecord] = for {
    // TODO check record is for current wallet
    recordWithTransactions <- Record.make(data)
    (record, transactions) = recordWithTransactions
    _ <- transaction(run(insertRecord(record)) *> run(insertTransactions(transactions))).orDie
  } yield record.toFull(transactions)

  override def get(id: Record.Id): WalletIO[RecordNotExist, FullRecord] = for {
    // TODO check record is for current wallet
    record       <- run(getRecord(id)).orDie.headOrError(RecordNotExist())
    transactions <- run(transactionsById(id)).orDie
  } yield record.toFull(transactions)

  override def update(id: Record.Id, data: RecordData): WalletUIO[FullRecord] = for {
    // TODO check record is for current wallet
    updatedRecord <- Record.make(id, data)
    (record, transactions) = updatedRecord
    _ <- transaction(
      run(transactionsById(id).delete) *> run(getRecord(id).delete) *>
        run(insertRecord(record)) *> run(insertTransactions(transactions))
    ).orDie
  } yield record.toFull(transactions)

  override def delete(id: Record.Id): WalletUIO[Unit] =
    // TODO check record is for current wallet and deletion result
    transaction(run(transactionsById(id).delete) *> run(getRecord(id).delete)).orDie.map(_ => ())

  override def list(account: Account.Id, pageToken: Option[Page.Token]): WalletUIO[Page[FullRecord]] = {
    val records = pageToken match {
      case Some(value) => run(selectFullRecords(account).page(PageSize, value)).orDie
      case None        => run(selectFullRecords(account).firstPage(PageSize)).orDie
    }

    records
      .map(_.map { case ((record, transaction1), transaction2) => record.toFull(transaction1, transaction2) })
      .map(r => page(r, r.size < PageSize))
  }

  override def balance(account: Account.Id): WalletUIO[List[AssetAmount]] = for {
    // TODO check account is for current wallet
    balances <- run(balanceByAccount(account)).orDie
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
