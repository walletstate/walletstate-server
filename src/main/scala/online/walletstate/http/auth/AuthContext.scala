package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import zio.*
import zio.http.*
import zio.json.*
import zio.json.internal.Write

import java.util.UUID

sealed trait AuthContext {
  def user: String
}

final case class UserContext(user: String) extends AuthContext

object UserContext {
  given codec: JsonCodec[UserContext] = DeriveJsonCodec.gen[UserContext]
}

final case class UserNamespaceContext(user: String, namespace: UUID) extends AuthContext

object UserNamespaceContext {
  given codec: JsonCodec[UserNamespaceContext] = DeriveJsonCodec.gen[UserNamespaceContext]
}

object AuthContext {

  given encoder: JsonEncoder[AuthContext] = (a: AuthContext, indent: Option[RuntimeFlags], out: Write) => {
    a match {
      case ctx: UserContext          => UserContext.codec.encoder.unsafeEncode(ctx, indent, out)
      case ctx: UserNamespaceContext => UserNamespaceContext.codec.encoder.unsafeEncode(ctx, indent, out)
    }
  }

  def of(userId: String, namespace: Option[UUID]): AuthContext =
    namespace.fold(UserContext(userId))(ns => UserNamespaceContext(userId, ns))

}
