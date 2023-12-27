package online.walletstate.db

import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy3, NamingStrategy, PluralizedTableNames, PostgresEscape, SnakeCase}
import online.walletstate.models.Group
import zio.ZLayer
import org.postgresql.util.PGobject

import java.sql.Types
import javax.sql.DataSource

class WalletStateQuillContext(override val ds: DataSource)
    extends Quill.Postgres[CompositeNamingStrategy3[SnakeCase, PluralizedTableNames, PostgresEscape]](
      NamingStrategy(io.getquill.SnakeCase, PluralizedTableNames, PostgresEscape),
      ds
    ) {

  given groupTypeEncoder: Encoder[Group.Type] = encoder[Group.Type](
    Types.OTHER,
    (index: Index, value: Group.Type, row: PrepareRow) => {
      val pgObj = new PGobject()
      pgObj.setType("group_type")
      pgObj.setValue(value.toString.toLowerCase)
      row.setObject(index, pgObj, Types.OTHER)
    }
  )

  given groupTypeDecoder: Decoder[Group.Type] =
    decoder((index, row, _) => Group.Type.valueOf(row.getObject(index).toString.capitalize))

}

object WalletStateQuillContext {
  val layer = ZLayer.fromFunction((ds: javax.sql.DataSource) => new WalletStateQuillContext(ds))
}
