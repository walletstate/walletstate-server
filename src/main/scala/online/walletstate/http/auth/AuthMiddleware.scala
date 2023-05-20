package online.walletstate.http.auth

import online.walletstate.services.auth.TokenService
import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import zio.*
import zio.json.*
import zio.http.*

case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag] =
    RequestHandlerMiddlewares.customAuthProvidingZIO[Any, Any, String, Ctx] { headers =>
      headers.getAuthCookies match {
        case None        => ZIO.succeed(None)
        case Some(value) => tokenService.decode(value).fold(_ => None, ctx => Some(ctx)) // todo add logging for errors
      }
    }
}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
