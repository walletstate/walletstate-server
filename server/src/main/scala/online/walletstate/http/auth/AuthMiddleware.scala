package online.walletstate.http.auth

import online.walletstate.common.models.HttpError
import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import online.walletstate.models.{AppError, AuthContext}
import online.walletstate.services.TokenService
import online.walletstate.utils.AuthCookiesOps.extractAuthToken
import online.walletstate.utils.RequestOps.outputMediaType
import zio.*
import zio.http.*
import zio.json.*

final case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag]: HandlerAspect[Any, Ctx] =
    Middleware.customAuthProvidingZIO[Any, Ctx] { request =>
      ZIO
        .fromOption(request.headers.extractAuthToken)
        .flatMap((token, tokenType) => tokenService.decodeAuthContext[Ctx](token, tokenType))
        .map(context => Some(context))
        .mapError {
          case None                         => AppError.NoToken.asUnauthorized.encode(request.outputMediaType)
          case e: AppError.TokenDecodeError => e.asUnauthorized.encode(request.outputMediaType)
          case _                            => HttpError.InternalServerError.default.encode(request.outputMediaType)
        }
    }

  val userCtx   = ctx[UserContext]
  val walletCtx = ctx[WalletContext]
}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
