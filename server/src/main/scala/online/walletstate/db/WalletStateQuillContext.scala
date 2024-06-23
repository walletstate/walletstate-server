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
import online.walletstate.common.models.{Account, Asset, Category, ExchangeRate, Group, Record, Transaction, Wallet, WalletUser}
import zio.{Duration, ZLayer}
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
      case Left(msg)    => throw new Exception(s"Cannot decode from DB type. Error: $msg")
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

  given transactionTypeEncoder: Encoder[Record.Type] =
    encoder[Record.Type](Types.OTHER, toDbType("record_type", Record.Type.asString))

  given transactionTypeDecoder: Decoder[Record.Type] =
    decoder(fromDbType(Record.Type.fromString))

  given durationEncoder: MappedEncoding[Duration, Long] = MappedEncoding[Duration, Long](_.toSeconds)
  given durationDecoder: MappedEncoding[Long, Duration] = MappedEncoding[Long, Duration](Duration.fromSeconds)

  object Tables {
    import io.getquill.*

    inline def Wallets: Quoted[EntityQuery[Wallet]]             = quote(query[Wallet])
    inline def WalletUsers: Quoted[EntityQuery[WalletUser]]     = quote(query[WalletUser])
    inline def Groups: Quoted[EntityQuery[Group]]               = quote(query[Group])
    inline def Accounts: Quoted[EntityQuery[Account]]           = quote(query[Account])
    inline def Categories: Quoted[EntityQuery[Category]]        = quote(querySchema[Category]("categories"))
    inline def Assets: Quoted[EntityQuery[Asset]]               = quote(query[Asset])
    inline def ExchangeRates: Quoted[EntityQuery[ExchangeRate]] = quote(query[ExchangeRate])
    inline def Records: Quoted[EntityQuery[Record]]             = quote(query[Record])
    inline def Transactions: Quoted[EntityQuery[Transaction]]   = quote(query[Transaction])

  }

}

object WalletStateQuillContext {
  val layer = ZLayer.fromFunction((ds: javax.sql.DataSource) => new WalletStateQuillContext(ds))
}
