package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.Analytics.GroupBy
import online.walletstate.models.AuthContext.WalletContext
import online.walletstate.models.{Account, Analytics, AssetAmount, Category, Group, Page, Record, Transaction}
import online.walletstate.services.queries.AnalyticsQuillQueries
import online.walletstate.{WalletUIO, models}
import zio.{ZIO, ZLayer}

import java.sql.SQLException

trait AnalyticsService {

  def records(filter: Analytics.Filter, page: Option[Page.Token]): WalletUIO[Page[Record.SingleTransaction]]
  def aggregate(request: Analytics.AggregateRequest): WalletUIO[List[AssetAmount]]
  def group(groupBy: Analytics.GroupRequest): WalletUIO[List[Analytics.GroupedResult]]
}

final case class AnalyticsServiceLive(quill: WalletStateQuillContext)
    extends AnalyticsService
    with AnalyticsQuillQueries {
  import io.getquill.*
  import quill.{*, given}

  private val PageSize = 50 // TODO Move to config

  override def records(filter: Analytics.Filter, page: Option[Page.Token]): WalletUIO[Page[Record.SingleTransaction]] =
    ZIO.serviceWithZIO[WalletContext] { ctx =>
      val query = selectRecords(ctx.wallet, filter, page, PageSize)
      translate(query).orDie.debug("[Analytics] Records query") *> // Remove
        run(query).orDie.map { (rows: List[(models.Record, Transaction)]) =>
          buildPage(rows.map((record, transaction) => record.toSingleTransaction(transaction.data)))
        }
    }

  override def aggregate(request: Analytics.AggregateRequest): WalletUIO[List[AssetAmount]] =
    ZIO.serviceWithZIO[WalletContext] { ctx =>
      val query =
        if (request.byFinalAsset) aggregateRecordsFinal(ctx.wallet, request.filter)
        else aggregateRecords(ctx.wallet, request.filter)

      translate(query).orDie.debug("[Analytics] Aggregate query ") *>
        run(query).orDie
    }

  override def group(groupBy: Analytics.GroupRequest): WalletUIO[List[Analytics.GroupedResult]] =
    ZIO.serviceWithZIO[WalletContext] { ctx =>
      val retrieveData = groupBy.groupBy match {
        case GroupBy.Category if groupBy.byFinalAsset      => run(groupByCategoryFinal(ctx.wallet, groupBy.filter))
        case GroupBy.Category                              => run(groupByCategory(ctx.wallet, groupBy.filter))
        case GroupBy.CategoryGroup if groupBy.byFinalAsset => run(groupByCategoryGroupFinal(ctx.wallet, groupBy.filter))
        case GroupBy.CategoryGroup                         => run(groupByCategoryGroup(ctx.wallet, groupBy.filter))
        case GroupBy.Account if groupBy.byFinalAsset       => run(groupByAccountFinal(ctx.wallet, groupBy.filter))
        case GroupBy.Account                               => run(groupByAccount(ctx.wallet, groupBy.filter))
        case GroupBy.AccountGroup if groupBy.byFinalAsset  => run(groupByAccountGroupFinal(ctx.wallet, groupBy.filter))
        case GroupBy.AccountGroup                          => run(groupByAccountGroup(ctx.wallet, groupBy.filter))
      }

      retrieveData.orDie.map { (list: List[(Category.Id | Account.Id | Group.Id, AssetAmount)]) =>
        list.groupBy(_._1).map((group, data) => Analytics.GroupedResult(group, data.map(_._2))).toList
      }
    }

  private def buildPage(records: List[Record.SingleTransaction]): Page[Record.SingleTransaction] = {
    if (records.length < PageSize) Page(records, None)
    else {
      val lastTwoRecords    = records.takeRight(2)
      val penultimateRecord = lastTwoRecords.head
      val lastRecord        = lastTwoRecords.last

      // TODO: redesign pagination
      // Currently, pagination is implemented based on record datetime and record id
      // For `SingleTransactionRecord` can exist two records with the same datetime and id (for `Transfer` record type)
      // So it can be a case, when the last record in the records list, is one of two records with the same datetime and id,
      // and the second record should be at the first place of the next page, but due to the current implementation of pagination,
      // it will be skipped.
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
