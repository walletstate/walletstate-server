package online.walletstate.services.queries

import online.walletstate.models.{Account, AssetAmount, Page, Record, Transaction}

trait RecordsQuillQueries extends QuillQueries {
  import quill.*
  import io.getquill.*
  import io.getquill.extras.ZonedDateTimeOps
  
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

  protected inline def insertRecord(record: Record): Insert[Record] =
    Tables.Records.insertValue(lift(record))

  protected inline def getRecord(id: Record.Id): EntityQuery[Record] =
    Tables.Records.filter(_.id == lift(id))

  protected inline def selectFullRecords(account: Account.Id): Query[((Record, Transaction), Option[Transaction])] =
    Tables.Records
      .join(Tables.Transactions)
      .on(_.id == _.id)
      .leftJoin(Tables.Transactions)
      .on { case ((record, t1), t2) => record.id == t2.id && (t1.account != t2.account || t1.asset != t2.asset) }
      .filter { case ((record, t1), t2) => t1.account == lift(account) }
      .distinctOn { case ((record, t1), t2) => (record.datetime, record.id) } // must be the same as sortBy

  protected inline def insertTransactions(transactions: List[Transaction]): Quoted[BatchAction[Insert[Transaction]]] =
    quote(liftQuery(transactions).foreach(t => Tables.Transactions.insertValue(t)))

  protected inline def transactionsById(id: Record.Id): EntityQuery[Transaction] =
    Tables.Transactions.filter(_.id == lift(id))

  protected inline def transactionsByAccount(account: Account.Id): Query[Transaction] =
    Tables.Transactions.filter(_.account == lift(account))

  protected inline def balanceByAccount(account: Account.Id): Query[AssetAmount] =
    Tables.Transactions
      .filter(_.account == lift(account))
      .groupByMap(_.asset)(t => AssetAmount(t.asset, sum(t.amount)))
}
