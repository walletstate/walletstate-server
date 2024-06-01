package online.walletstate.models

import zio.Chunk
import zio.schema.{derived, Schema}

final case class Transaction(id: Record.Id, account: Account.Id, asset: Asset.Id, amount: BigDecimal) derives Schema {
  def data: Transaction.Data = Transaction.Data(account, asset, amount)
  def isFrom: Boolean       = amount.signum < 0
  def isTo: Boolean         = !isFrom
}

object Transaction {

  def make(id: Record.Id, from: Option[Data], to: Option[Data]): List[Transaction] = {
    val fromTransaction = from.map(t => Transaction(id, t.account, t.asset, -(t.amount.abs))) // from always negative
    val toTransaction   = to.map(t => Transaction(id, t.account, t.asset, t.amount.abs))      // to always positive
    List(fromTransaction, toTransaction).flatten
  }

  final case class Data(account: Account.Id, asset: Asset.Id, amount: BigDecimal) derives Schema
}
