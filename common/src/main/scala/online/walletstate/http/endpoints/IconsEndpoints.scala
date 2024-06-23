package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.Icon
import zio.http.codec.{HttpCodec, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait IconsEndpoints extends WalletStateEndpoints {

  val listEndpoint =
    Endpoint(Method.GET / "api" / "icons")
      .@@(EndpointAuthorization)
      .query(QueryCodec.query("tag").optional)
      .out[List[Icon.Id]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val createEndpoint =
    Endpoint(Method.POST / "api" / "icons")
      .@@(EndpointAuthorization)
      .in[Icon.Data]
      .out[Icon.Id](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map("list" -> listEndpoint, "create" -> createEndpoint)
}

object IconsEndpoints extends IconsEndpoints
