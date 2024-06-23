package online.walletstate.http.endpoints

import online.walletstate.common.models.{AuthToken, HttpError, Wallet, WalletInvite}
import zio.http.codec.{Doc, HttpCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait WalletsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "wallets")
      .in[Wallet.Data]
      .out[Wallet](Status.Created)
      .outError[HttpError.NotFound](HttpError.NotFound.status, Doc.p("Something strange. User not found"))
      .withBadRequestCodec

  val getCurrent =
    Endpoint(Method.GET / "api" / "wallets" / "current").withBadRequestCodec
      .out[Wallet]
      .outError[HttpError.NotFound](HttpError.NotFound.status, Doc.p("Wallet not found"))

  val createInvite =
    Endpoint(Method.POST / "api" / "wallets" / "invite")
      .out[WalletInvite](Status.Created)
      .outError[HttpError.NotFound](HttpError.NotFound.status, Doc.p("Wallet not found"))
      .withBadRequestCodec

  val join =
    Endpoint(Method.POST / "api" / "wallets" / "join")
      .in[Wallet.Join]
      .out[Wallet]
      .outErrors[HttpError.NotFound | HttpError.Forbidden](
        HttpCodec.error[HttpError.NotFound](HttpError.NotFound.status),
        HttpCodec.error[HttpError.Forbidden](HttpError.Forbidden.status) ?? Doc.p("Invite expired")
      )
      .withBadRequestCodec

  val createApiToken =
    Endpoint(Method.POST / "api" / "wallets" / "token")
      .in[AuthToken.Create]
      .out[AuthToken](Status.Created)
      .outError[HttpError.Forbidden](HttpError.Forbidden.status)
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create"         -> create,
    "getCurrent"     -> getCurrent,
    "createInvite"   -> createInvite,
    "join"           -> join,
    "createApiToken" -> createApiToken
  )
}

object WalletsEndpoints extends WalletsEndpoints
