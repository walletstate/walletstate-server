package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import online.walletstate.services.TokenService
import online.walletstate.models.errors.{AppError, AuthTokenNotFound}
import zio.*
import zio.http.*
import zio.json.*

case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag] =
    RequestHandlerMiddlewares.customAuthProvidingZIO[Any, Any, AppError, Ctx] { headers =>
      headers.getAuthCookies match {
        case None        => ZIO.fail(AuthTokenNotFound)
        case Some(value) => tokenService.decode(value).map(Some(_))
      }
    }
}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
