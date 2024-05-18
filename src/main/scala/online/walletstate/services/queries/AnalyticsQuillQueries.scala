package online.walletstate.services.queries

import online.walletstate.models
import online.walletstate.models.{Record, Account, Analytics, Asset, Category, Group, Page, Transaction, Wallet}

trait AnalyticsQuillQueries extends QuillQueries {
  import quill.{*, given}
  import io.getquill.*
  import io.getquill.extras.ZonedDateTimeOps

  protected def selectRecords(
      wallet: Wallet.Id,
      filter: Analytics.Filter,
      pageToken: Option[Page.Token],
      pageSize: Int
  ) = {
    quote(joinedRecord).dynamic // TODO Investigate SQL injections for dynamic queries
      .filterRecords(wallet, filter, applyEnd = pageToken.isEmpty)
      .getPage(filter, pageSize, pageToken)
  }

  // TODO Refactor to avoid unnecessary joins
  private inline def joinedRecord: Query[(Record, Transaction, Account, Group, Asset, Category)] =
    Tables.Records
      .join(Tables.Categories)
      .on(_.category == _.id)
      .join(transactionWithAccountAndAsset)
      .on { case ((record, _), (transaction, _, _, _)) => record.id == transaction.id }
      .map { case ((record, category), (transaction, asset, account, accountGroup)) =>
        (record, transaction, account, accountGroup, asset, category)
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
  //// Extensions
  ////////////////////////////////////////////////
  extension (recordsQuery: DynamicQuery[(Record, Transaction, Account, Group, Asset, Category)]) {
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
        .filterIf(f.assetTypes.nonEmpty)     (row => quote(liftQuery(f.assetTypes).contains(row.asset.`type`)))
        .filterIf(f.assetTag.nonEmpty)       (row => quote(row.asset.tags.contains(lift(f.assetTag.get))))
        .filterIf(f.spentOn.nonEmpty)        (row => quote(liftQuery(f.spentOn).contains(row.record.spentOn)))
        .filterIf(f.generatedBy.nonEmpty)    (row => quote(liftQuery(f.generatedBy).contains(row.record.generatedBy)))
    }
    // format: on

    private def getPage(filter: Analytics.Filter, size: Int, page: Option[Page.Token]) = {
      def startPageFilter(row: Quoted[(Record, Transaction, Account, Group, Asset, Category)], p: Page.Token) = {
        quote(row.record.datetime < lift(p.dt) || (row.record.datetime == lift(p.dt) && row.record.id > lift(p.id)))
      }

      recordsQuery
        .filterIf(page.nonEmpty)(row => startPageFilter(row, page.get))
        // .filterOpt(page)((row, p) => startPageFilter(row, p)) //Investigate: GenericEncoder is needed
        .sortBy(row => (row.record.datetime, row.record.id))(Ord(Ord.desc, Ord.asc))
        .map(row => (row.record, row.transaction))
        .take(size)
    }
  }

  // Simple extension to make the code above more readable
  extension (row: Quoted[(Record, Transaction, Account, Group, Asset, Category)]) {
    private inline def record       = row._1
    private inline def transaction  = row._2
    private inline def account      = row._3
    private inline def accountGroup = row._4
    private inline def asset        = row._5
    private inline def category     = row._6
  }

}
