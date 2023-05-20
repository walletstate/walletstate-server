package online.walletstate.http

import online.walletstate.domain.auth.LoginInfo
import online.walletstate.http.RequestOps.as
import online.walletstate.http.auth.AuthCookiesOps.*
import online.walletstate.http.auth.AuthRoutesHandler
import online.walletstate.services.auth.TokenService
import zio.*
import zio.http.*

final case class AuthRoutes(authRoutesHandler: AuthRoutesHandler) {

  private val login = Http.collectZIO[Request] { case req @ Method.POST -> !! / "login" =>
    authRoutesHandler.login(req)
  }

  private val logout = Http.collectZIO[Request] { case req @ Method.GET -> !! / "logout" =>
    authRoutesHandler.logout(req)
  }

  def routes = login ++ logout

}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
