package online.walletstate.http

import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import zio.http.Routes

trait WalletStateRoutes {

  val noCtxRoutes: Routes[Any, _]            = Routes.empty
  val userRoutes: Routes[UserContext, _]     = Routes.empty
  val walletRoutes: Routes[WalletContext, _] = Routes.empty

}
