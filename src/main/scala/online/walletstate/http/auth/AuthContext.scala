package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import online.walletstate.models.{Namespace, User}
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

final case class UserNamespaceContext(user: User.Id, namespace: Namespace.Id) extends AuthContext

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

  def of(userId: User.Id, namespace: Option[Namespace.Id]): AuthContext =
    namespace.fold(UserContext(userId))(ns => UserNamespaceContext(userId, ns))

}
