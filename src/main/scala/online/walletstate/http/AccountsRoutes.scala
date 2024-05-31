package online.walletstate.http

import online.walletstate.http.api.AccountsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.api.{CreateAccount, UpdateAccount}
import online.walletstate.models.{Account, AppError, Page, Transaction}
import online.walletstate.services.{AccountsService, RecordsService}
import zio.*
import zio.http.*

case class AccountsRoutes(
    auth: AuthMiddleware,
    accountsService: AccountsService,
    recordsService: RecordsService
) extends AccountsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateAccount, WalletContext)] {
    Handler.fromFunctionZIO((accInfo, ctx) => accountsService.create(ctx.wallet, ctx.user, accInfo))
  }()

  private val listRoute = list.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => accountsService.list(ctx.wallet))
  }()

  private val listGroupedRoute = listGrouped.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => accountsService.grouped(ctx.wallet))
  }()

  private val getRoute = get.implementWithWalletCtx[(Account.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => accountsService.get(ctx.wallet, id))
  }()

  private val updateRoute = update.implementWithWalletCtx[(Account.Id, UpdateAccount, WalletContext)] {
    Handler.fromFunctionZIO((id, info, ctx) => accountsService.update(ctx.wallet, id, info))
  }()

  private val listRecordsRoute = listRecords.implementWithWalletCtx[(Account.Id, Option[Page.Token], WalletContext)] {
    Handler.fromFunctionZIO((id, token, ctx) => recordsService.list(ctx.wallet, id, token))
  }()

  private val getBalanceRoute = getBalance.implementWithWalletCtx[(Account.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => recordsService.balance(ctx.wallet, id))
  }()

  def routes = Routes(
    createRoute,
    listGroupedRoute,
    getRoute,
    updateRoute,
    listRoute,
    listRecordsRoute,
    getBalanceRoute
  )
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
