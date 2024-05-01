package online.walletstate.models

import online.walletstate.models.api.TransactionData
import zio.Chunk
import zio.schema.{DeriveSchema, Schema}

final case class Transaction(id: Record.Id, account: Account.Id, asset: Asset.Id, amount: BigDecimal) {
  def data: TransactionData = TransactionData(account, asset, amount)
  def isFrom: Boolean = amount.signum < 0
  def isTo: Boolean = !isFrom
}

object Transaction {

  def make(id: Record.Id, from: Option[TransactionData], to: Option[TransactionData]): List[Transaction] = {
    val fromTransaction = from.map(t => Transaction(id, t.account, t.asset, -(t.amount.abs))) // from always negative
    val toTransaction   = to.map(t => Transaction(id, t.account, t.asset, t.amount.abs))      // to always positive
    List(fromTransaction, toTransaction).flatten
  }
  
  given schema: Schema[Transaction] = DeriveSchema.gen[Transaction]
}
