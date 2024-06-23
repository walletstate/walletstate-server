package online.walletstate.http

import online.walletstate.http.endpoints.WalletStateEndpoints
import zio.ZLayer
import zio.http.*

final case class HealthRoutes() extends WalletStateRoutes with WalletStateEndpoints {
  private val health = Routes(Method.GET / "health" -> handler(Response.text("alive")))

  private val version = Routes(Method.GET / "version" -> handler(Response.text("0.0.1")))

  override val noCtxRoutes = health ++ version
}

object HealthRoutes {
  val layer = ZLayer.fromFunction(HealthRoutes.apply _)
}
