package online.walletstate.services.queries

import online.walletstate.common.models.Record
import online.walletstate.db.WalletStateQuillContext

trait QuillQueries {
  val quill: WalletStateQuillContext

  // TODO needs for `quote(transaction.id <= lift(id))`. Investigate more general options for AnyVal
  implicit class RecordIdOrdered(val value: Record.Id) extends Ordered[Record.Id] {
    override def compare(that: Record.Id): Int = value.id.compareTo(that.id)
  }
}
