package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, Unauthorized}
import online.walletstate.common.models.Icon
import zio.http.codec.{HttpCodec, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait IconsEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Icons"

  val listEndpoint =
    Endpoint(Method.GET / "api" / "icons").walletStateEndpoint
      .query(QueryCodec.query[String]("tag").optional)
      .out[List[Icon.Id]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val createEndpoint =
    Endpoint(Method.POST / "api" / "icons").walletStateEndpoint
      .in[Icon.Data]
      .out[Icon.Id](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  override val endpointsMap = Map("list" -> listEndpoint, "create" -> createEndpoint)
}

object IconsEndpoints extends IconsEndpoints
