package online.walletstate.http.auth

import online.walletstate.http.auth.AuthCookiesOps.getAuthCookies
import EndpointOps.implementWithCtx
import online.walletstate.services.TokenService
import online.walletstate.models.errors.{AppError, AuthTokenNotFound}
import zio.*
import zio.http.*
import zio.http.endpoint.Endpoint
import zio.json.*

import scala.reflect.ClassTag

final case class AuthMiddleware(tokenService: TokenService) {

  def ctx[Ctx <: AuthContext: JsonDecoder: Tag]: HandlerAspect[Any, Ctx] =
    Middleware.customAuthProvidingZIO[Any, Ctx] { request =>
      request.headers.getAuthCookies match {
        case None        => ZIO.succeed(None)
        case Some(value) => tokenService.decode(value).map(Some(_)).catchAll(_ => ZIO.succeed(None))
      }
    }

  val userCtx   = ctx[UserContext]
  val walletCtx = ctx[WalletContext]

  extension [EPatIn, EIn, EErr: ClassTag, EOut](endpoint: Endpoint[EPatIn, EIn, EErr, EOut, _])
    def implementWithWalletCtx[HIn](
        handler: Handler[Any, Any, HIn, EOut]
    )(
        errorMapper: PartialFunction[Any, EErr] = PartialFunction.empty
    )(using z: Zippable.Out[EIn, WalletContext, HIn], trace: Trace): Route[Any, Any] =
      endpoint.implementWithCtx[WalletContext, HIn](walletCtx)(handler)(errorMapper)

    def implementWithUserCtx[HIn](
        handler: Handler[Any, Any, HIn, EOut]
    )(
        errorMapper: PartialFunction[Any, EErr] = PartialFunction.empty
    )(using z: Zippable.Out[EIn, UserContext, HIn], trace: Trace): Route[Any, Any] =
      endpoint.implementWithCtx[UserContext, HIn](userCtx)(handler)(errorMapper)

}

object AuthMiddleware {
  val layer: ZLayer[TokenService, Nothing, AuthMiddleware] = ZLayer.fromFunction(AuthMiddleware.apply _)
}
