package online.walletstate

import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy2, NamingStrategy, PluralizedTableNames, SnakeCase}

package object db {
  type QuillCtx = Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]]

  val QuillNamingStrategy: CompositeNamingStrategy2[SnakeCase, PluralizedTableNames] =
    NamingStrategy(io.getquill.SnakeCase, PluralizedTableNames)
}
