package online.walletstate.db

import io.getquill.jdbczio.Quill
import io.getquill.{
  CompositeNamingStrategy3,
  MappedEncoding,
  NamingStrategy,
  PluralizedTableNames,
  PostgresEscape,
  SnakeCase
}
import online.walletstate.models.{Account, Asset, Category, ExchangeRate, Group, Record}
import zio.ZLayer
import org.postgresql.util.PGobject

import java.sql.Types
import javax.sql.DataSource

class WalletStateQuillContext(override val ds: DataSource)
    extends Quill.Postgres[CompositeNamingStrategy3[SnakeCase, PluralizedTableNames, PostgresEscape]](
      NamingStrategy(io.getquill.SnakeCase, PluralizedTableNames, PostgresEscape),
      ds
    ) {

  private def toDbType[T](dbType: String, toString: T => String)(index: Index, value: T, row: PrepareRow) = {
    val pgObj = new PGobject()
    pgObj.setType(dbType)
    pgObj.setValue(toString(value))
    row.setObject(index, pgObj, Types.OTHER)
  }

  private def fromDbType[T](fromString: String => Either[String, T])(index: Int, row: ResultRow, session: Session) = {
    fromString(row.getObject(index).toString) match {
      case Right(value) => value
      case Left(msg)    =>
        // TODO: investigate how to avoid throwing exception
        throw new Exception(s"Cannot decode from DB type. Error: $msg")
    }
  }

  given groupTypeEncoder: Encoder[Group.Type] =
    encoder[Group.Type](Types.OTHER, toDbType("group_type", Group.Type.asString))

  given groupTypeDecoder: Decoder[Group.Type] =
    decoder(fromDbType(Group.Type.fromString))

  given assetTypeEncoder: Encoder[Asset.Type] =
    encoder[Asset.Type](Types.OTHER, toDbType("asset_type", Asset.Type.asString))

  given assetTypeDecoder: Decoder[Asset.Type] =
    decoder(fromDbType(Asset.Type.fromString))

  // mappers todo: make as enum in db
  given recordTypeEncoder: MappedEncoding[Record.Type, String] =
    MappedEncoding[Record.Type, String](_.toString.toLowerCase)
  given recordTypeDecoder: MappedEncoding[String, Record.Type] =
    MappedEncoding[String, Record.Type](s => Record.Type.valueOf(s.capitalize))

  object Tables {
    import io.getquill.*

    inline def Groups: Quoted[EntityQuery[Group]]               = quote(query[Group])
    inline def Accounts: Quoted[EntityQuery[Account]]           = quote(query[Account])
    inline def Categories: Quoted[EntityQuery[Category]]        = quote(querySchema[Category]("categories"))
    inline def Assets: Quoted[EntityQuery[Asset]]               = quote(query[Asset])
    inline def ExchangeRates: Quoted[EntityQuery[ExchangeRate]] = quote(query[ExchangeRate])

  }

}

object WalletStateQuillContext {
  val layer = ZLayer.fromFunction((ds: javax.sql.DataSource) => new WalletStateQuillContext(ds))
}
