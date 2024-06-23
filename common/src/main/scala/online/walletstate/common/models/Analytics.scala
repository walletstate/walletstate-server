package online.walletstate.common.models

import zio.schema.{derived, Schema}

import java.time.ZonedDateTime
import java.util.UUID

final case class Analytics()

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
      assetGroups: Set[Group.Id] = Set.empty,
      assetTypes: Set[Asset.Type] = Set.empty,
      assetTag: Option[String] = None, // allow filtering only by one tag for now
      spentOn: Set[Asset.Id] = Set.empty,
      generatedBy: Set[Asset.Id] = Set.empty
  ) derives Schema
  
  final case class AggregateRequest(filter: Filter, byFinalAsset: Boolean = false) derives Schema
  
  // TODO To not allow server and DB overloading:
  // - define and implement validation rules (like allow groupBy category only if categoryGroup or category is defined in filter)
  // - limit the max number of groups, categories and accounts that can be created for wallet
  final case class GroupRequest(filter: Filter, groupBy: GroupBy, byFinalAsset: Boolean = false) derives Schema

  enum GroupBy {
    case Category, CategoryGroup, Account, AccountGroup
  }

  // Keep group with UUID type for now.
  // TODO investigate how to use generic (or union) type with zio-http-endpoint
  // or refactor model
  final case class GroupedResult private (group: UUID, assets: List[AssetAmount]) derives Schema

  object GroupedResult {

    // TODO investigate Value Class instantiation in this case and how to avoid it
    def apply(group: Category.Id | Account.Id | Group.Id, assets: List[AssetAmount]): GroupedResult = group match {
      case g: Category.Id => GroupedResult(g.id, assets)
      case g: Account.Id  => GroupedResult(g.id, assets)
      case g: Group.Id    => GroupedResult(g.id, assets)
    }
  }
  
}
