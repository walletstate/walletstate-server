package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.AppError.TransactionNotExist
import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.{Account, Asset, AssetBalance, Transaction, Wallet}
import zio.{Task, ZIO, ZLayer}

trait TransactionsService {
  def create(wallet: Wallet.Id, info: CreateTransaction): Task[List[Transaction]]
  def get(wallet: Wallet.Id, id: Transaction.Id): Task[List[Transaction]]
  def list(wallet: Wallet.Id, account: Account.Id, pageToken: Option[Transaction.Page.Token]): Task[Transaction.Page]
  def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetBalance]]
}

case class TransactionsServiceLive(quill: WalletStateQuillContext) extends TransactionsService {
  import io.getquill.*
  import quill.{*, given}
  import io.getquill.extras.ZonedDateTimeOps

  private inline val PageSize = 50 // TODO Move to configs

  override def create(wallet: Wallet.Id, info: CreateTransaction): Task[List[Transaction]] = for {
    // TODO check all assets and accounts are in the wallet
    transactions <- Transaction.make(info)
    _            <- run(insert(transactions))
  } yield transactions

  override def get(wallet: Wallet.Id, id: Transaction.Id): Task[List[Transaction]] = for {
    transactions <- run(transactionsById(id))
    // TODO check transaction is for current wallet
    _ <- if (transactions.isEmpty) ZIO.fail(TransactionNotExist) else ZIO.unit
  } yield transactions

  override def list(
      wallet: Wallet.Id,
      account: Account.Id,
      pageToken: Option[Transaction.Page.Token]
  ): Task[Transaction.Page] = for {
    // TODO check account is for current wallet
    transactions <- runList(account, pageToken)
  } yield Transaction.page(transactions.take(PageSize), transactions.size == PageSize)

  // TODO investigate using dynamic queries instead
  private def runList(account: Account.Id, pageToken: Option[Transaction.Page.Token]) = {
    pageToken match {
      case Some(token) => run(transactionsByAccount(account).page(PageSize, token))
      case None        => run(transactionsByAccount(account).firstPage(PageSize))
    }
  }

  override def balance(wallet: Wallet.Id, account: Account.Id): Task[List[AssetBalance]] = for {
    // TODO check account is for current wallet
    balances <- run(balanceByAccount(account))
  } yield balances

  // queries utils
  // TODO needs for `quote(transaction.id <= lift(id))`. Investigate more general options for AnyVal
  implicit class TransactionIdOrdered(val value: Transaction.Id) extends Ordered[Transaction.Id] {
    override def compare(that: Transaction.Id): Index = value.id.compareTo(that.id)
  }

  extension (transactionsQuery: Query[Transaction]) {
    inline def firstPage(inline pageSize: Int): Query[Transaction] = quote {
      transactionsQuery
        .sortBy(t => (t.datetime, t.id))(Ord(Ord.desc, Ord.asc))
        .take(pageSize)
    }

    inline def page(inline pageSize: Int, inline pageToken: Transaction.Page.Token): Query[Transaction] = quote {
      transactionsQuery
        .filter(t => t.datetime < lift(pageToken.dt) || (t.datetime == lift(pageToken.dt) && t.id > lift(pageToken.id)))
        .sortBy(t => (t.datetime, t.id))(Ord(Ord.desc, Ord.asc))
        .take(pageSize)
    }
  }

  // queries
  private inline def insert(transactions: Seq[Transaction]) =
    quote(liftQuery(transactions).foreach(t => Tables.Transactions.insertValue(t)))

  private inline def transactionsById(id: Transaction.Id) = Tables.Transactions.filter(_.id == lift(id))

  private inline def transactionsByAccount(account: Account.Id): Query[Transaction] =
    Tables.Transactions.filter(_.account == lift(account))

  private inline def balanceByAccount(account: Account.Id) =
    Tables.Transactions.filter(_.account == lift(account)).groupByMap(_.asset)(t => AssetBalance(t.asset, sum(t.amount)))
}

object TransactionsServiceLive {
  val layer = ZLayer.fromFunction(TransactionsServiceLive.apply _)
}
