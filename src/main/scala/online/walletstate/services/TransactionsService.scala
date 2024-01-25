package online.walletstate.services

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.errors.TransactionNotExist
import online.walletstate.models.{Account, Asset, Category, Group, Transaction, User, Wallet}
import online.walletstate.utils.ZIOExtensions.getOrError
import zio.{Task, ZIO, ZLayer}

trait TransactionsService {
  def create(wallet: Wallet.Id, info: CreateTransaction): Task[Seq[Transaction]]
  def get(wallet: Wallet.Id, id: Transaction.Id): Task[Seq[Transaction]]
  def list(wallet: Wallet.Id, account: Account.Id): Task[Seq[Transaction]]
  def balance(wallet: Wallet.Id, account: Account.Id): Task[Map[Asset.Id, BigDecimal]]
}

case class TransactionsServiceLive(quill: WalletStateQuillContext) extends TransactionsService {
  import io.getquill.*
  import quill.{*, given}

  override def create(wallet: Wallet.Id, info: CreateTransaction): Task[Seq[Transaction]] = for {
    // TODO check all assets and accounts are in the wallet
    transactions <- Transaction.make(info)
    _            <- run(insert(transactions))
  } yield transactions

  override def get(wallet: Wallet.Id, id: Transaction.Id): Task[Seq[Transaction]] = for {
    transactions <- run(transactionsById(id))
    // TODO check transaction is for current wallet
    _ <- if (transactions.isEmpty) ZIO.fail(TransactionNotExist) else ZIO.unit
  } yield transactions

  override def list(wallet: Wallet.Id, account: Account.Id): Task[Seq[Transaction]] =
    for {
      // TODO check account is for current wallet
      transactions <- run(transactionsByAccount(account))
      _            <- if (transactions.isEmpty) ZIO.fail(TransactionNotExist) else ZIO.unit
    } yield transactions

  override def balance(wallet: Wallet.Id, account: Account.Id): Task[Map[Asset.Id, BigDecimal]] = for {
    // TODO check account is for current wallet
    balances <- run(balanceByAccount(account))
  } yield balances.toMap

  // queries
  private inline def insert(transactions: Seq[Transaction]) =
    quote(liftQuery(transactions).foreach(t => Tables.Transactions.insertValue(t)))

  private inline def transactionsById(id: Transaction.Id) = Tables.Transactions.filter(_.id == lift(id))

  private inline def transactionsByAccount(account: Account.Id) =
    Tables.Transactions.filter(_.account == lift(account)).sortBy(_.datetime)(Ord.desc)

  private inline def balanceByAccount(account: Account.Id) =
    Tables.Transactions.filter(_.account == lift(account)).groupByMap(_.asset)(t => (t.asset, sum(t.amount)))
}

object TransactionsServiceLive {
  val layer = ZLayer.fromFunction(TransactionsServiceLive.apply _)
}
