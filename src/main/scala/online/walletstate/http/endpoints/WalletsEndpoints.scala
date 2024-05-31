package online.walletstate.http.endpoints

import online.walletstate.models.AppError.{UserNotExist, WalletInviteExpired, WalletInviteNotExist, WalletNotExist}
import online.walletstate.models.api.{CreateWallet, JoinWallet}
import online.walletstate.models.{AppError, Wallet, WalletInvite}
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.Endpoint
import zio.http.endpoint.EndpointMiddleware.None
import zio.http.{Method, Status}

trait WalletsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "wallets")
      .in[CreateWallet]
      .out[Wallet](Status.Created)
      .outError[UserNotExist](Status.NotFound, Doc.p("Something strange. User not found"))

  val getCurrent =
    Endpoint(Method.GET / "api" / "wallets" / "current")
      .out[Wallet]
      .outError[WalletNotExist](Status.NotFound, Doc.p("Wallet not found"))

  val createInvite =
    Endpoint(Method.POST / "api" / "wallets" / "invite")
      .out[WalletInvite](Status.Created)
      .outError[WalletNotExist](Status.NotFound, Doc.p("Wallet not found"))

  val join =
    Endpoint(Method.POST / "api" / "wallets" / "join")
      .in[JoinWallet]
      .out[Wallet]
      .outErrors[UserNotExist | WalletInviteNotExist | WalletInviteExpired | WalletNotExist](
        HttpCodec.error[UserNotExist](Status.NotFound) ?? Doc.p("Something strange. User not found"),
        HttpCodec.error[WalletInviteNotExist](Status.NotFound) ?? Doc.p("Invite not found"),
        HttpCodec.error[WalletInviteExpired](Status.Forbidden) ?? Doc.p("Invite expired"),
        HttpCodec.error[WalletNotExist](Status.NotFound) ?? Doc.p("Something strange. Wallet not found")
      )

  override val endpointsMap = Map(
    "create"       -> create,
    "getCurrent"   -> getCurrent,
    "createInvite" -> createInvite,
    "join"         -> join
  )
}

object WalletsEndpoints extends WalletsEndpoints
