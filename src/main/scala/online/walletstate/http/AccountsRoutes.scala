package online.walletstate.http

import online.walletstate.http.api.endpoints.AccountsEndpoints
import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.api.CreateAccount
import online.walletstate.models.{Account, Transaction}
import online.walletstate.models.errors.AccountNotExist
import online.walletstate.services.{AccountsService, TransactionsService}
import zio.*
import zio.http.*

case class AccountsRoutes(
    auth: AuthMiddleware,
    accountsService: AccountsService,
    transactionsService: TransactionsService
) extends AccountsEndpoints {
  import auth.implementWithWalletCtx

  private val createRoute = create.implementWithWalletCtx[(CreateAccount, WalletContext)] {
    Handler.fromFunctionZIO((accInfo, ctx) => accountsService.create(ctx.wallet, ctx.user, accInfo))
  }()

  private val listRoute = list.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => accountsService.list(ctx.wallet).map(Chunk.from))
  }()

  private val listGroupedRoute = listGrouped.implementWithWalletCtx[WalletContext] {
    Handler.fromFunctionZIO(ctx => accountsService.grouped(ctx.wallet).map(Chunk.from))
  }()

  private val getRoute = get.implementWithWalletCtx[(Account.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => accountsService.get(ctx.wallet, id))
  } { case e: AccountNotExist => Right(e) }

  private val listTransactionsRoute =
    listTransactions.implementWithWalletCtx[(Account.Id, Option[Transaction.Page.Token], WalletContext)] {
      Handler.fromFunctionZIO((id, token, ctx) => transactionsService.list(ctx.wallet, id, token))
    }()

  private val getBalanceRoute = getBalance.implementWithWalletCtx[(Account.Id, WalletContext)] {
    Handler.fromFunctionZIO((id, ctx) => transactionsService.balance(ctx.wallet, id))
  }()

  def routes = Routes(
    createRoute,
    listGroupedRoute,
    getRoute,
    listRoute,
    listTransactionsRoute,
    getBalanceRoute
  )
}

object AccountsRoutes {
  val layer = ZLayer.fromFunction(AccountsRoutes.apply _)
}
