package online.walletstate.models

import zio.schema.{DeriveSchema, Schema}

import java.time.ZonedDateTime

final case class Analytics(filter: Analytics.Filter)

object Analytics {

  // TODO Add validation which filters can be used simultaneously
  final case class Filter(
      start: Option[ZonedDateTime] = None,
      end: Option[ZonedDateTime] = None,
      recordTypes: Set[Record.Type] = Set.empty,
      recordTag: Option[String] = None, // allow filtering only by one tag for now
      categories: Set[Category.Id] = Set.empty,
      categoryGroups: Set[Group.Id] = Set.empty,
      categoryTag: Option[String] = None, // allow filtering only by one tag for now
      accounts: Set[Account.Id] = Set.empty,
      accountGroups: Set[Group.Id] = Set.empty,
      accountTag: Option[String] = None, // allow filtering only by one tag for now
      assets: Set[Asset.Id] = Set.empty,
      assetTypes: Set[Asset.Type] = Set.empty,
      assetTag: Option[String] = None, // allow filtering only by one tag for now
      spentOn: Set[Asset.Id] = Set.empty,
      generatedBy: Set[Asset.Id] = Set.empty
  )

  object Filter {
    given schema: Schema[Filter] = DeriveSchema.gen
  }

  given schema: Schema[Analytics] = DeriveSchema.gen
}
