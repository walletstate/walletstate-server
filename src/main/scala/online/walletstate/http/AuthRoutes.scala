package online.walletstate.http

import online.walletstate.http.auth.AuthRoutesHandler
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
