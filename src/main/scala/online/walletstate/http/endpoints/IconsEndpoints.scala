package online.walletstate.http.endpoints

import online.walletstate.models.{HttpError, Icon}
import zio.http.codec.QueryCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait IconsEndpoints extends WalletStateEndpoints {

  val list =
    Endpoint(Method.GET / "api" / "icons")
      .query(QueryCodec.query("tag").optional)
      .out[List[Icon.Id]]
      .withBadRequestCodec

  val create =
    Endpoint(Method.POST / "api" / "icons")
      .in[Icon.Data]
      .out[Icon.Id](Status.Created)
      .withBadRequestCodec

  override val endpointsMap = Map("list" -> list, "create" -> create)
}

object IconsEndpoints extends IconsEndpoints
