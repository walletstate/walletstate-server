package online.walletstate.db

import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy3, MappedEncoding, NamingStrategy, PluralizedTableNames, PostgresEscape, SnakeCase}
import online.walletstate.models.{Group, Record}
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
      pgObj.setValue(Group.Type.asString(value))
      row.setObject(index, pgObj, Types.OTHER)
    }
  )

  given groupTypeDecoder: Decoder[Group.Type] =
    decoder((index, row, _) => Group.Type.fromString(row.getObject(index).toString).toOption.get) //TODO rewrite .toOption.get


  // mappers todo: make as enum in db
  given recordTypeEncoder: MappedEncoding[Record.Type, String] = MappedEncoding[Record.Type, String](_.toString.toLowerCase)
  given recordTypeDecoder: MappedEncoding[String, Record.Type] = MappedEncoding[String, Record.Type](s => Record.Type.valueOf(s.capitalize))

}

object WalletStateQuillContext {
  val layer = ZLayer.fromFunction((ds: javax.sql.DataSource) => new WalletStateQuillContext(ds))
}
