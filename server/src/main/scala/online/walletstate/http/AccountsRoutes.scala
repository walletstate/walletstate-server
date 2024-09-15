package online.walletstate.http

import online.walletstate.http.endpoints.AccountsEndpoints
import online.walletstate.services.{AccountsService, RecordsService}
import zio.*
import zio.http.*

case class AccountsRoutes(accountsService: AccountsService, recordsService: RecordsService)
    extends WalletStateRoutes
    with AccountsEndpoints {

  private val createRoute      = createEndpoint.implement(accInfo => accountsService.create(accInfo))
  private val listRoute        = listEndpoint.implement(_ => accountsService.list)
  private val listGroupedRoute = listGroupedEndpoint.implement(_ => accountsService.grouped)
  private val getRoute         = getEndpoint.implement(id => accountsService.get(id).mapError(_.asNotFound))
  private val updateRoute      = updateEndpoint.implement((id, info) => accountsService.update(id, info))
  private val listRecordsRoute = listRecordsEndpoint.implement((id, token) => recordsService.list(id, token))
  private val getBalanceRoute  = getBalanceEndpoint.implement(id => recordsService.balance(id))

  override val walletRoutes = Routes(
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
