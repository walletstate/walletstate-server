package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models
import online.walletstate.models.api.SingleTransactionRecord
import online.walletstate.models.{Analytics, Page, Transaction, Wallet, Record}
import online.walletstate.services.queries.AnalyticsQuillQueries
import zio.{Task, ZIO, ZLayer}

trait AnalyticsService {

  def records(
      wallet: Wallet.Id,
      filter: Analytics.Filter,
      page: Option[Page.Token]
  ): Task[Page[SingleTransactionRecord]]
}

final case class AnalyticsServiceLive(quill: WalletStateQuillContext)
    extends AnalyticsService
    with AnalyticsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  private val PageSize = 50 // TODO Move to config

  override def records(
      wallet: Wallet.Id,
      filter: Analytics.Filter,
      page: Option[Page.Token]
  ): Task[Page[SingleTransactionRecord]] = {
    val query = selectRecords(wallet, filter, page, PageSize)
    translate(query).debug("[Analytics] Records query") *> // Remove
      run(query).map { (rows: List[(models.Record, Transaction)]) =>
        buildPage(rows.map((record, transaction) => record.toSingleTransaction(transaction.data)))
      }
  }

  private def buildPage(records: List[SingleTransactionRecord]): Page[SingleTransactionRecord] = {
    if (records.length < PageSize) Page(records, None)
    else {
      val lastTwoRecords    = records.takeRight(2)
      val penultimateRecord = lastTwoRecords.head
      val lastRecord        = lastTwoRecords.last

      // TODO: redesign pagination
      // Currently, pagination is implemented based on record datetime and record id
      // For `SingleTransactionRecord` can exist two records with the same datetime and id (for `Transfer` record type)
      // So it can be a case, when the last record in the records list, is one of two records with the same datetime and id,
      // and the second record should be at the first place of the next page, but due to the current implementation of pagination, it will be skipped.
      // As a workaround added validation of the latest element (dropped latest `Transfer` record to load both on the next page)
      if (lastRecord.`type` == Record.Type.Transfer && lastRecord.id != penultimateRecord.id) {
        Page(records.dropRight(1), Some(Page.Token(penultimateRecord.id, penultimateRecord.datetime)))
      } else {
        Page(records, Some(Page.Token(lastRecord.id, lastRecord.datetime)))
      }
    }
  }
}

object AnalyticsServiceLive {
  val layer = ZLayer.fromFunction(AnalyticsServiceLive.apply _)
}
