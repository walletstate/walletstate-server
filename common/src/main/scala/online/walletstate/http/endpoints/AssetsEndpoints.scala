package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Asset, Grouped}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, Status}

trait AssetsEndpoints extends WalletStateEndpoints {

  val createEndpoint =
    Endpoint(Method.POST / "api" / "assets")
      .auth(AuthType.Bearer)
      .in[Asset.Data]
      .out[Asset](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val listGroupedEndpoint =
    Endpoint(Method.GET / "api" / "assets" / "grouped")
      .auth(AuthType.Bearer)
      .out[List[Grouped[Asset]]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val listEndpoint =
    Endpoint(Method.GET / "api" / "assets")
      .auth(AuthType.Bearer)
      .out[List[Asset]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val getEndpoint =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path)
      .auth(AuthType.Bearer)
      .out[Asset]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "assets" / Asset.Id.path)
      .auth(AuthType.Bearer)
      .in[Asset.Data]
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create"      -> createEndpoint,
    "get"         -> getEndpoint,
    "update"      -> updateEndpoint,
    "list"        -> listEndpoint,
    "listGrouped" -> listGroupedEndpoint
  )

  override val endpoints = Chunk(
    createEndpoint,
    getEndpoint,
    updateEndpoint,
//    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    listEndpoint
  )
}

object AssetsEndpoints extends AssetsEndpoints
