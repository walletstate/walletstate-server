package online.walletstate.http

import online.walletstate.http.auth.AuthRoutesHandler
import zio.*
import zio.http.*

final case class AuthRoutes(authRoutesHandler: AuthRoutesHandler) {

  def routes = Routes(
    Method.POST / "auth" / "login"  -> handler { (req: Request) => authRoutesHandler.login(req) },
    Method.POST / "auth" / "logout" -> handler { (req: Request) => authRoutesHandler.logout(req) }
  )

}

object AuthRoutes {
  val layer = ZLayer.fromFunction(AuthRoutes.apply _)
}
