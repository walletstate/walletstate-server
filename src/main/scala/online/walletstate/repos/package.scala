package online.walletstate

import io.getquill.{CompositeNamingStrategy2, NamingStrategy, PluralizedTableNames, SnakeCase}
import io.getquill.jdbczio.Quill

package object repos {
  type QuillCtx = Quill.Postgres[CompositeNamingStrategy2[SnakeCase, PluralizedTableNames]]

  val QuillNamingStrategy: CompositeNamingStrategy2[SnakeCase, PluralizedTableNames] =
    NamingStrategy(io.getquill.SnakeCase, PluralizedTableNames)
}
