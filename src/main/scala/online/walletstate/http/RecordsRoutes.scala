package online.walletstate.http

import online.walletstate.http.api.RecordsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models
import online.walletstate.models.api.RecordData
import online.walletstate.models.{Account, AppError, Page, Record}
import online.walletstate.services.RecordsService
import zio.*
import zio.http.*

case class RecordsRoutes(auth: AuthMiddleware, recordsService: RecordsService) extends RecordsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(RecordData, WalletContext)] {
    Handler.fromFunctionZIO((info, ctx) => recordsService.create(ctx.wallet, info))
  }()

  private val listRoute = list.implementWithWalletCtx[(Account.Id, Option[Page.Token], WalletContext)] {
    Handler.fromFunctionZIO((account, nextPageToken, ctx) => recordsService.list(ctx.wallet, account, nextPageToken))
  }()

  private val getRoute = get.implementWithWalletCtx[(Record.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => recordsService.get(ctx.wallet, id))
  }()

  private val updateRoute = update.implementWithWalletCtx[(Record.Id, RecordData, WalletContext)] {
    Handler.fromFunctionZIO((id, data, ctx) => recordsService.update(ctx.wallet, id, data))
  }()

  private val deleteRoute = delete.implementWithWalletCtx[(Record.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => recordsService.delete(ctx.wallet, id))
  }()

  def routes = Routes(createRoute, listRoute, getRoute, updateRoute, deleteRoute)
}

object RecordsRoutes {
  val layer = ZLayer.fromFunction(RecordsRoutes.apply _)
}
