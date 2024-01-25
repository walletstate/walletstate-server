package online.walletstate.models.api

import online.walletstate.models.{Account, Asset, Category, Transaction}
import zio.json.{DeriveJsonCodec, JsonCodec}

import java.time.ZonedDateTime

//TODO Rename or refactor whole model
final case class AccountAssetAmount(account: Account.Id, asset: Asset.Id, amount: BigDecimal) {

  def toAmount: BigDecimal   = if (amount < 0) amount * -1 else amount //income should be positive
  def fromAmount: BigDecimal = if (amount > 0) amount * -1 else amount //spending should be negative
}

object AccountAssetAmount {
  given codec: JsonCodec[AccountAssetAmount] = DeriveJsonCodec.gen[AccountAssetAmount]
}

final case class CreateTransaction(
    `type`: Transaction.Type,
    from: Option[AccountAssetAmount],
    to: Option[AccountAssetAmount],
    category: Category.Id,
    datetime: ZonedDateTime,
    description: Option[String],
    tags: Seq[String],
    externalId: Option[String],
    spentOn: Option[Asset.Id],
    generatedBy: Option[Asset.Id]
)

object CreateTransaction {
  given codec: JsonCodec[CreateTransaction] = DeriveJsonCodec.gen[CreateTransaction]
}
