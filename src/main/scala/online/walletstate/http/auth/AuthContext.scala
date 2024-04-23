package online.walletstate.http.auth

import online.walletstate.models.{User, Wallet}
import zio.*
import zio.json.*
import zio.json.internal.Write
import zio.schema.{DeriveSchema, Schema}

sealed trait AuthContext {
  def user: User.Id
}

final case class UserContext(user: User.Id) extends AuthContext

object UserContext {
  given schema: Schema[UserContext]   = DeriveSchema.gen[UserContext]
  given codec: JsonCodec[UserContext] = zio.schema.codec.JsonCodec.jsonCodec(schema)
}

final case class WalletContext(user: User.Id, wallet: Wallet.Id) extends AuthContext

object WalletContext {
  given schema: Schema[WalletContext]   = DeriveSchema.gen[WalletContext]
  given codec: JsonCodec[WalletContext] = zio.schema.codec.JsonCodec.jsonCodec(schema)
}

object AuthContext {

  given encoder: JsonEncoder[AuthContext] = (a: AuthContext, indent: Option[RuntimeFlags], out: Write) => {
    a match {
      case ctx: UserContext   => UserContext.codec.encoder.unsafeEncode(ctx, indent, out)
      case ctx: WalletContext => WalletContext.codec.encoder.unsafeEncode(ctx, indent, out)
    }
  }

  def of(user: User.Id, wallet: Option[Wallet.Id]): AuthContext =
    wallet.fold(UserContext(user))(ns => WalletContext(user, ns))

}
