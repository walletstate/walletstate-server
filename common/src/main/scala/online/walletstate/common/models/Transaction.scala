package online.walletstate.common.models

import zio.Chunk
import zio.schema.{derived, Schema}

final case class Transaction(
    record: Record.Id,
    account: Account.Id,
    asset: Asset.Id,
    wallet: Wallet.Id,
    amount: BigDecimal
) derives Schema {
  def data: Transaction.Data = Transaction.Data(account, asset, amount)
  def isFrom: Boolean        = amount.signum < 0
  def isTo: Boolean          = !isFrom
}

object Transaction {

  def make(wallet: Wallet.Id, record: Record.Id, from: Option[Data], to: Option[Data]): List[Transaction] = {
    val fromTransaction =
      from.map(t => Transaction(record, t.account, t.asset, wallet, -(t.amount.abs))) // from always negative
    val toTransaction =
      to.map(t => Transaction(record, t.account, t.asset, wallet, t.amount.abs)) // to always positive
    List(fromTransaction, toTransaction).flatten
  }

  final case class Data(account: Account.Id, asset: Asset.Id, amount: BigDecimal) derives Schema
}
