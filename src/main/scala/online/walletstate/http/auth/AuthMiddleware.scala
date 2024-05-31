package online.walletstate.http.auth

import online.walletstate.models.{AppError, AuthContext}
import online.walletstate.utils.AuthCookiesOps.{getAuthCookies, getBearerToken}
import online.walletstate.models.AppError.Unauthorized
import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import online.walletstate.services.TokenService
import online.walletstate.utils.RequestOps.outputMediaType
import zio.*
import zio.http.*
import zio.json.*

final case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag]: HandlerAspect[Any, Ctx] =
    Middleware.customAuthProvidingZIO[Any, Ctx] { request =>
      request.headers.getAuthCookies
        .orElse(request.headers.getBearerToken) // temporary implementation. TODO: implement feature for API tokens
        .fold(ZIO.fail(Unauthorized.authTokenNotFound))(tokenStr => tokenService.decode(tokenStr))
        .map(token => Some(token))
        .mapError {
          case e: Unauthorized =>
            AppError.UnauthorizedCodec.encodeResponse(e, request.outputMediaType)
          case e =>
            AppError.InternalServerErrorCodec.encodeResponse(AppError.InternalServerError, request.outputMediaType)
        }
    }

  val userCtx   = ctx[UserContext]
  val walletCtx = ctx[WalletContext]
}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
