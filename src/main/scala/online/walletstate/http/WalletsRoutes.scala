package online.walletstate.http

import online.walletstate.http.endpoints.WalletsEndpoints
import online.walletstate.models.AppError.{UserNotExist, WalletInviteExpired, WalletInviteNotExist, WalletNotExist}
import online.walletstate.models.HttpError
import online.walletstate.services.WalletsService
import zio.*
import zio.http.*

final case class WalletsRoutes(walletsService: WalletsService) extends WalletStateRoutes with WalletsEndpoints {

  private val createRoute = create.implement {
    Handler.fromFunctionZIO(info => walletsService.create(info.name).mapError(HttpError.NotFound.apply))
  }

  private val getCurrentRoute = getCurrent.implement {
    Handler.fromFunctionZIO(_ => walletsService.get.mapError(HttpError.NotFound.apply))
  }

  private val createInviteRoute = createInvite.implement {
    Handler.fromFunctionZIO(_ => walletsService.createInvite.mapError(HttpError.NotFound.apply))
  }

  private val joinRoute = join.implement {
    Handler.fromFunctionZIO { info =>
      walletsService.joinWallet(info.inviteCode).mapError {
        case e: UserNotExist         => HttpError.NotFound(e)
        case e: WalletNotExist       => HttpError.NotFound(e)
        case e: WalletInviteNotExist => HttpError.NotFound(e)
        case e: WalletInviteExpired  => HttpError.Forbidden(e)
      }
    }
  }

  override val userRoutes   = Routes(createRoute, joinRoute)
  override val walletRoutes = Routes(getCurrentRoute, createInviteRoute)
}

object WalletsRoutes {
  val layer = ZLayer.fromFunction(WalletsRoutes.apply _)
}
