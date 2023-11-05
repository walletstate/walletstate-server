package online.walletstate

import io.getquill.jdbczio.Quill
import io.getquill.{CompositeNamingStrategy3, NamingStrategy, PluralizedTableNames, PostgresEscape, SnakeCase}

package object db {
  type QuillCtx = Quill.Postgres[CompositeNamingStrategy3[SnakeCase, PluralizedTableNames, PostgresEscape]]

  val QuillNamingStrategy: CompositeNamingStrategy3[SnakeCase, PluralizedTableNames, PostgresEscape] =
    NamingStrategy(io.getquill.SnakeCase, PluralizedTableNames, PostgresEscape)
}
