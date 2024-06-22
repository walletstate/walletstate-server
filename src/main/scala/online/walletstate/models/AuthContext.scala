package online.walletstate.models

import zio.*
import zio.json.*
import zio.json.internal.Write
import zio.schema.{DeriveSchema, Schema}

sealed trait AuthContext {
  def user: User.Id
  def `type`: AuthContext.Type
}

object AuthContext {

  enum Type {
    case Cookies, Bearer
  }

  final case class UserContext(user: User.Id, `type`: AuthContext.Type) extends AuthContext

  object UserContext {
    given schema: Schema[UserContext]   = DeriveSchema.gen[UserContext]
    given codec: JsonCodec[UserContext] = zio.schema.codec.JsonCodec.jsonCodec(schema)
  }

  final case class WalletContext(user: User.Id, wallet: Wallet.Id, `type`: AuthContext.Type) extends AuthContext

  object WalletContext {
    given schema: Schema[WalletContext]   = DeriveSchema.gen[WalletContext]
    given codec: JsonCodec[WalletContext] = zio.schema.codec.JsonCodec.jsonCodec(schema)
  }

  given encoder: JsonEncoder[AuthContext] = (a: AuthContext, indent: Option[RuntimeFlags], out: Write) => {
    a match {
      case ctx: UserContext   => UserContext.codec.encoder.unsafeEncode(ctx, indent, out)
      case ctx: WalletContext => WalletContext.codec.encoder.unsafeEncode(ctx, indent, out)
    }
  }

  def of(user: User.Id, wallet: Option[Wallet.Id], `type`: Type): AuthContext =
    wallet.fold(UserContext(user, `type`))(wallet => WalletContext(user, wallet, `type`))
}
