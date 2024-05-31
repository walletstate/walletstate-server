package online.walletstate.http

import online.walletstate.http.endpoints.WalletsEndpoints
import online.walletstate.services.WalletsService
import zio.*
import zio.http.*

final case class WalletsRoutes(walletsService: WalletsService) extends WalletStateRoutes with WalletsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => walletsService.create(info.name))
  }

  private val getCurrentRoute = getCurrent.implement {
    Handler.fromFunctionZIO(_ => walletsService.get)
  }

  private val createInviteRoute = createInvite.implement {
    Handler.fromFunctionZIO(_ => walletsService.createInvite)
  }

  private val joinRoute = join.implement {
    Handler.fromFunctionZIO(info => walletsService.joinWallet(info.inviteCode))
  }

  override val userRoutes   = Routes(createRoute, joinRoute)
  override val walletRoutes = Routes(getCurrentRoute, createInviteRoute)
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
