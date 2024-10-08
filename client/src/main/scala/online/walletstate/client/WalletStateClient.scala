package online.walletstate.client

import online.walletstate.client.configs.WalletStateClientConfig
import online.walletstate.http.endpoints.WalletStateEndpoints.Auth.ClientAuthRequirement
import zio.ZLayer
import zio.http.endpoint.EndpointExecutor

case class WalletStateClient(executor: EndpointExecutor[Any, ClientAuthRequirement]) {
  val accounts: AccountsClient           = AccountsClient(executor)
  val analytics: AnalyticsClient         = AnalyticsClient(executor)
  val assets: AssetsClient               = AssetsClient(executor)
  val categories: CategoriesClient       = CategoriesClient(executor)
  val exchangeRates: ExchangeRatesClient = ExchangeRatesClient(executor)
  val groups: GroupsClient               = GroupsClient(executor)
  val icons: IconsClient                 = IconsClient(executor)
  val records: RecordsClient             = RecordsClient(executor)
  val wallets: WalletsClient             = WalletsClient(executor)
}

object WalletStateClient {
  val layer = WalletStateClientConfig.configuredEndpointExecutorLayer >>> ZLayer.fromFunction(WalletStateClient.apply _)
}
