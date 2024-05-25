package online.walletstate.services.queries

import online.walletstate.db.WalletStateQuillContext
import online.walletstate.models.Record

trait QuillQueries {
  val quill: WalletStateQuillContext

  // TODO needs for `quote(transaction.id <= lift(id))`. Investigate more general options for AnyVal
  implicit class RecordIdOrdered(val value: Record.Id) extends Ordered[Record.Id] {
    override def compare(that: Record.Id): Int = value.id.compareTo(that.id)
  }
}
