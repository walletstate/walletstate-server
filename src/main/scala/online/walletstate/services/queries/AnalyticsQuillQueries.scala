package online.walletstate.services.queries

import online.walletstate.models
import online.walletstate.models.{
  Account,
  Analytics,
  Asset,
  AssetAmount,
  Category,
  Group,
  Page,
  Record,
  Transaction,
  Wallet
}

trait AnalyticsQuillQueries extends QuillQueries {
  import quill.{*, given}
  import io.getquill.*
  import io.getquill.extras.ZonedDateTimeOps
  import AnalyticsQuillQueries.RecordRow

  protected def selectRecords(wallet: Wallet.Id, filter: Analytics.Filter, page: Option[Page.Token], pageSize: Int) =
    filteredRecordRows(wallet, filter, applyEnd = page.isEmpty).getPage(filter, pageSize, page)

  protected def aggregateRecords(wallet: Wallet.Id, filter: Analytics.Filter) =
    filteredRecordRows(wallet, filter).groupByAsset

  protected def groupByCategory(wallet: Wallet.Id, filter: Analytics.Filter) =
    filteredRecordRows(wallet, filter).groupByCategoryAndAsset

  protected def groupByCategoryGroup(wallet: Wallet.Id, filter: Analytics.Filter) =
    filteredRecordRows(wallet, filter).groupByCategoryGroupAndAsset

  protected def groupByAccount(wallet: Wallet.Id, filter: Analytics.Filter) =
    filteredRecordRows(wallet, filter).groupByAccountAndAsset

  protected def groupByAccountGroup(wallet: Wallet.Id, filter: Analytics.Filter) =
    filteredRecordRows(wallet, filter).groupByAccountGroupAndAsset

  private def filteredRecordRows(wallet: Wallet.Id, filter: Analytics.Filter, applyEnd: Boolean = true) =
    quote(joinedRecord).dynamic // TODO Investigate SQL injections for dynamic queries
      .filterRecords(wallet, filter, applyEnd)

  // TODO Refactor to avoid unnecessary joins
  private inline def joinedRecord: Query[RecordRow] =
    Tables.Records
      .join(Tables.Categories)
      .on(_.category == _.id)
      .join(transactionWithAccountAndAsset)
      .on { case ((record, _), (transaction, _, _, _)) => record.id == transaction.id }
      .map { case ((record, category), (transaction, asset, account, accountGroup)) =>
        RecordRow(record, transaction, account, accountGroup, asset, category)
      }

  private inline def transactionWithAccountAndAsset: Query[(Transaction, Asset, Account, Group)] =
    Tables.Transactions
      .join(Tables.Assets)
      .on(_.asset == _.id)
      .join(accountWithGroup)
      .on { case ((transaction, _), (account, _)) => transaction.account == account.id }
      .map { case ((transaction, asset), (account, group)) => (transaction, asset, account, group) }

  private inline def accountWithGroup: Query[(Account, Group)] =
    Tables.Accounts.join(Tables.Groups).on(_.group == _.id)

  ////////////////////////////////////////////////
  //// Extensions/Helpers
  ////////////////////////////////////////////////
  private inline def sumAmount(query: Query[RecordRow]) =
    quote(query.map(_.transaction.amount).sum.getOrElse(lift(BigDecimal(0))))

  extension (recordsQuery: DynamicQuery[RecordRow]) {
    // format: off
    private def filterRecords(wallet: Wallet.Id, f: Analytics.Filter, applyEnd: Boolean = true) = {
      recordsQuery.filter(_.accountGroup.wallet == lift(wallet))
        .filterIf(f.start.nonEmpty)          (row => quote(row.record.datetime >= lift(f.start.get)))
        .filterIf(f.end.nonEmpty && applyEnd)(row => quote(row.record.datetime <= lift(f.end.get)))
        .filterIf(f.recordTypes.nonEmpty)    (row => quote(liftQuery(f.recordTypes).contains(row.record.`type`)))
        .filterIf(f.recordTag.nonEmpty)      (row => quote(row.record.tags.contains(lift(f.recordTag.get))))
        .filterIf(f.categories.nonEmpty)     (row => quote(liftQuery(f.categories).contains(row.record.category)))
        .filterIf(f.categoryGroups.nonEmpty) (row => quote(liftQuery(f.categoryGroups).contains(row.category.group)))
        .filterIf(f.categoryTag.nonEmpty)    (row => quote(row.category.tags.contains(lift(f.categoryTag.get))))
        .filterIf(f.accounts.nonEmpty)       (row => quote(liftQuery(f.accounts).contains(row.transaction.account)))
        .filterIf(f.accountGroups.nonEmpty)  (row => quote(liftQuery(f.accountGroups).contains(row.account.group)))
        .filterIf(f.accountTag.nonEmpty)     (row => quote(row.account.tags.contains(lift(f.accountTag.get))))
        .filterIf(f.assets.nonEmpty)         (row => quote(liftQuery(f.assets).contains(row.transaction.asset)))
        .filterIf(f.assetGroups.nonEmpty)    (row => quote(liftQuery(f.assetGroups).contains(row.asset.group)))
        .filterIf(f.assetTypes.nonEmpty)     (row => quote(liftQuery(f.assetTypes).contains(row.asset.`type`)))
        .filterIf(f.assetTag.nonEmpty)       (row => quote(row.asset.tags.contains(lift(f.assetTag.get))))
        .filterIf(f.spentOn.nonEmpty)        (row => quote(liftQuery(f.spentOn).contains(row.record.spentOn)))
        .filterIf(f.generatedBy.nonEmpty)    (row => quote(liftQuery(f.generatedBy).contains(row.record.generatedBy)))
    }
    // format: on

    private def getPage(filter: Analytics.Filter, size: Int, page: Option[Page.Token]) = {
      def startPageFilter(row: Quoted[RecordRow], p: Page.Token) = {
        quote(row.record.datetime < lift(p.dt) || (row.record.datetime == lift(p.dt) && row.record.id > lift(p.id)))
      }

      recordsQuery
        .filterIf(page.nonEmpty)(row => startPageFilter(row, page.get))
        // .filterOpt(page)((row, p) => startPageFilter(row, p)) //Investigate: GenericEncoder is needed
        .sortBy(row => (row.record.datetime, row.record.id))(Ord(Ord.desc, Ord.asc))
        .map(row => (row.record, row.transaction))
        .take(size)
    }

    private def groupByAsset =
      recordsQuery
        .groupBy(_.transaction.asset)
        .map(grouped => AssetAmount(grouped._1, sumAmount(grouped._2)))

    private def groupByCategoryAndAsset =
      recordsQuery
        .groupBy(row => (row.record.category, row.transaction.asset))
        .map(grouped => (grouped._1._1, AssetAmount(grouped._1._2, sumAmount(grouped._2))))

    private def groupByCategoryGroupAndAsset =
      recordsQuery
        .groupBy(row => (row.category.group, row.transaction.asset))
        .map(grouped => (grouped._1._1, AssetAmount(grouped._1._2, sumAmount(grouped._2))))

    private def groupByAccountAndAsset =
      recordsQuery
        .groupBy(row => (row.transaction.account, row.transaction.asset))
        .map(grouped => (grouped._1._1, AssetAmount(grouped._1._2, sumAmount(grouped._2))))

    private def groupByAccountGroupAndAsset =
      recordsQuery
        .groupBy(row => (row.account.group, row.transaction.asset))
        .map(grouped => (grouped._1._1, AssetAmount(grouped._1._2, sumAmount(grouped._2))))

  }

}

object AnalyticsQuillQueries {
  private case class RecordRow(
      record: Record,
      transaction: Transaction,
      account: Account,
      accountGroup: Group,
      asset: Asset,
      category: Category
  )
}
