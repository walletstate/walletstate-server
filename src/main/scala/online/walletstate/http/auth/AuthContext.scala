package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import online.walletstate.models.{Wallet, User}
import zio.*
import zio.http.*
import zio.json.*
import zio.json.internal.Write

sealed trait AuthContext {
  def user: User.Id
}

final case class UserContext(user: User.Id) extends AuthContext

object UserContext {
  given codec: JsonCodec[UserContext] = DeriveJsonCodec.gen[UserContext]
}

final case class WalletContext(user: User.Id, wallet: Wallet.Id) extends AuthContext

object WalletContext {
  given codec: JsonCodec[WalletContext] = DeriveJsonCodec.gen[WalletContext]
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
