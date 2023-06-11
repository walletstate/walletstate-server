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

  def routes = Http.collectZIO[Request] {
    case req @ Method.POST -> !! / "auth" / "login"  => authRoutesHandler.login(req)
    case req @ Method.POST -> !! / "auth" / "logout" => authRoutesHandler.logout(req)
  }

}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
