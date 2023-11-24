package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import online.walletstate.services.TokenService
import online.walletstate.models.errors.{AppError, AuthTokenNotFound}
import zio.*
import zio.http.*
import zio.json.*

case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag]: HandlerAspect[Any, Ctx] =
    Middleware.customAuthProvidingZIO[Any, Ctx] { request =>
      request.headers.getAuthCookies match {
//        case None        => ZIO.fail(AuthTokenNotFound) //TODO Investigate how to response with custom errors
        case None        => ZIO.succeed(None)
        case Some(value) => tokenService.decode(value).map(Some(_)).catchAll(_ => ZIO.succeed(None))
      }
    }

  val userCtx   = ctx[UserContext]
  val walletCtx = ctx[WalletContext]
}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
