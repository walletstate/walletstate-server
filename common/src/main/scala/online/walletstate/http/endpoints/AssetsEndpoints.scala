package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Asset, Grouped}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AssetsEndpoints extends WalletStateEndpoints {
  
  override protected final val tag: String = "Assets"

  val createEndpoint =
    Endpoint(Method.POST / "api" / "assets").walletStateEndpoint
      .in[Asset.Data]
      .out[Asset](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listGroupedEndpoint =
    Endpoint(Method.GET / "api" / "assets" / "grouped").walletStateEndpoint
      .out[List[Grouped[Asset]]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listEndpoint =
    Endpoint(Method.GET / "api" / "assets").walletStateEndpoint
      .out[List[Asset]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val getEndpoint =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path).walletStateEndpoint
      .out[Asset]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "assets" / Asset.Id.path).walletStateEndpoint
      .in[Asset.Data]
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

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
